package br.com.commercecore.pricing;

import br.com.commercecore.catalog.CatalogService;
import br.com.commercecore.catalog.SkuSnapshot;
import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.pricing.internal.PriceTableEntity;
import br.com.commercecore.pricing.internal.PriceTableItemEntity;
import br.com.commercecore.pricing.internal.PriceTableRepository;
import br.com.commercecore.pricing.internal.PromotionEntity;
import br.com.commercecore.pricing.internal.PromotionRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

    private static final BigDecimal CEM = new BigDecimal("100");

    private final PriceTableRepository tabelas;
    private final PromotionRepository promocoes;
    private final CatalogService catalogo;
    private final BranchAccessService acessoFilial;
    private final Clock relogio;

    public PricingService(
            PriceTableRepository tabelas,
            PromotionRepository promocoes,
            CatalogService catalogo,
            BranchAccessService acessoFilial,
            Clock relogio) {
        this.tabelas = tabelas;
        this.promocoes = promocoes;
        this.catalogo = catalogo;
        this.acessoFilial = acessoFilial;
        this.relogio = relogio;
    }

    @Transactional
    public PriceTableResponse criarTabela(CreatePriceTableRequest requisicao) {
        validarPeriodo(requisicao.vigenteDe(), requisicao.vigenteAte());
        if (requisicao.filialId() != null) {
            var filial = acessoFilial.garantirAcesso(requisicao.filialId());
            if (!filial.empresaId().equals(requisicao.empresaId())) {
                throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
            }
        }

        PriceTableEntity tabela = new PriceTableEntity(
                requisicao.empresaId(), requisicao.filialId(), requisicao.nome().trim(),
                requisicao.vigenteDe(), requisicao.vigenteAte());

        var ids = requisicao.itens().stream().map(PriceTableItemRequest::skuId).toList();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new BusinessRuleException("SKU_REPETIDO_NA_TABELA", "Cada SKU pode aparecer somente uma vez na tabela.");
        }

        for (PriceTableItemRequest item : requisicao.itens()) {
            SkuSnapshot sku = catalogo.obterSku(item.skuId());
            garantirMesmaEmpresa(requisicao.empresaId(), sku);
            tabela.adicionarItem(new PriceTableItemEntity(
                    item.skuId(), Dinheiro.de(item.valorVenda()), Dinheiro.de(item.custoReferencia())));
        }
        return resposta(tabelas.save(tabela));
    }

    @Transactional
    public PromotionResponse criarPromocao(CreatePromotionRequest requisicao) {
        validarPeriodo(requisicao.inicio(), requisicao.fim());
        SkuSnapshot sku = catalogo.obterSku(requisicao.skuId());
        garantirMesmaEmpresa(requisicao.empresaId(), sku);
        if (requisicao.filialId() != null) {
            var filial = acessoFilial.garantirAcesso(requisicao.filialId());
            if (!filial.empresaId().equals(requisicao.empresaId())) {
                throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
            }
        }
        if (requisicao.tipoDesconto() == DiscountType.PERCENTUAL
                && requisicao.valorDesconto().compareTo(CEM) > 0) {
            throw new BusinessRuleException("PERCENTUAL_INVALIDO", "O desconto percentual nao pode superar 100%.");
        }
        PromotionEntity promocao = new PromotionEntity(
                requisicao.empresaId(), requisicao.filialId(), requisicao.skuId(), requisicao.nome().trim(),
                normalizarCupom(requisicao.codigoCupom()), requisicao.tipoDesconto(), requisicao.valorDesconto(),
                requisicao.quantidadeMinima(), requisicao.inicio(), requisicao.fim(), requisicao.prioridade());
        return resposta(promocoes.save(promocao));
    }

    @Transactional(readOnly = true)
    public PriceCalculation calcular(PriceSimulationRequest requisicao) {
        acessoFilial.garantirAcesso(requisicao.filialId());
        SkuSnapshot sku = catalogo.obterSku(requisicao.skuId());
        garantirMesmaEmpresa(requisicao.empresaId(), sku);
        validarQuantidade(requisicao.quantidade(), sku);

        Instant instante = requisicao.instanteReferencia() == null ? relogio.instant() : requisicao.instanteReferencia();
        PriceTableEntity tabela = tabelas.buscarAplicaveis(
                        requisicao.empresaId(), requisicao.filialId(), requisicao.skuId(), instante).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException(
                        "PRECO_NAO_CADASTRADO", "Nao existe preco vigente para o SKU e filial informados."));
        PriceTableItemEntity item = tabela.getItens().stream()
                .filter(candidato -> candidato.getSkuId().equals(requisicao.skuId()))
                .findFirst()
                .orElseThrow();

        String cupom = normalizarCupom(requisicao.codigoCupom());
        PromotionEntity promocao = melhorPromocao(
                requisicao, instante, cupom, item.getValorVenda());
        Dinheiro desconto = promocao == null
                ? Dinheiro.zero()
                : desconto(promocao, item.getValorVenda());
        Dinheiro precoFinal = item.getValorVenda().subtrair(desconto).maximoZero();
        Dinheiro subtotal = precoFinal.multiplicar(requisicao.quantidade());
        BigDecimal margem = calcularMargem(precoFinal, item.getCustoReferencia());

        return new PriceCalculation(
                tabela.getId(), tabela.getNome(), requisicao.skuId(), requisicao.quantidade(), item.getValorVenda(),
                desconto, precoFinal, subtotal, item.getCustoReferencia(), margem,
                promocao == null ? null : promocao.getId(), promocao == null ? null : promocao.getNome(),
                promocao == null ? null : promocao.getCodigoCupom());
    }

    private PromotionEntity melhorPromocao(
            PriceSimulationRequest requisicao, Instant instante, String cupom, Dinheiro precoBase) {
        return promocoes.buscarAplicaveis(
                        requisicao.empresaId(), requisicao.filialId(), requisicao.skuId(), requisicao.quantidade(),
                        instante, cupom).stream()
                .max(Comparator
                        .comparing((PromotionEntity promocao) -> desconto(promocao, precoBase))
                        .thenComparingInt(PromotionEntity::getPrioridade))
                .orElse(null);
    }

    private Dinheiro desconto(PromotionEntity promocao, Dinheiro precoBase) {
        Dinheiro calculado = promocao.getTipoDesconto() == DiscountType.PERCENTUAL
                ? precoBase.percentual(promocao.getValorDesconto())
                : Dinheiro.de(promocao.getValorDesconto());
        return calculado.minimo(precoBase);
    }

    private BigDecimal calcularMargem(Dinheiro preco, Dinheiro custo) {
        if (!preco.positivo()) {
            return BigDecimal.ZERO.setScale(2);
        }
        return preco.subtrair(custo).valor()
                .multiply(CEM)
                .divide(preco.valor(), 2, RoundingMode.HALF_UP);
    }

    private void garantirMesmaEmpresa(UUID empresaId, SkuSnapshot sku) {
        if (!sku.empresaId().equals(empresaId)) {
            throw new BusinessRuleException("SKU_DE_OUTRA_EMPRESA", "O SKU nao pertence a empresa informada.");
        }
    }

    private void validarQuantidade(BigDecimal quantidade, SkuSnapshot sku) {
        if (quantidade.signum() <= 0) {
            throw new BusinessRuleException("QUANTIDADE_INVALIDA", "A quantidade deve ser maior que zero.");
        }
        if (!sku.aceitaFracionado() && quantidade.stripTrailingZeros().scale() > 0) {
            throw new BusinessRuleException("SKU_NAO_FRACIONADO", "Este SKU aceita apenas quantidades inteiras.");
        }
    }

    private void validarPeriodo(Instant inicio, Instant fim) {
        if (fim != null && !fim.isAfter(inicio)) {
            throw new BusinessRuleException("PERIODO_INVALIDO", "O fim da vigencia deve ser posterior ao inicio.");
        }
    }

    private String normalizarCupom(String cupom) {
        return cupom == null || cupom.isBlank() ? null : cupom.trim().toUpperCase();
    }

    private static PriceTableResponse resposta(PriceTableEntity tabela) {
        return new PriceTableResponse(
                tabela.getId(), tabela.getEmpresaId(), tabela.getFilialId(), tabela.getNome(), tabela.getVigenteDe(),
                tabela.getVigenteAte(), tabela.isAtiva(), tabela.getItens().stream()
                        .map(item -> new PriceTableItemResponse(
                                item.getSkuId(), item.getValorVenda(), item.getCustoReferencia()))
                        .toList());
    }

    private static PromotionResponse resposta(PromotionEntity promocao) {
        return new PromotionResponse(
                promocao.getId(), promocao.getEmpresaId(), promocao.getFilialId(), promocao.getSkuId(),
                promocao.getNome(), promocao.getCodigoCupom(), promocao.getTipoDesconto(),
                promocao.getValorDesconto(), promocao.getQuantidadeMinima(), promocao.getInicio(), promocao.getFim(),
                promocao.getPrioridade(), promocao.isAtiva());
    }
}
