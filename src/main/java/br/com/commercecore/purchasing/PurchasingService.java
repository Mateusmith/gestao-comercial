package br.com.commercecore.purchasing;

import br.com.commercecore.catalog.CatalogService;
import br.com.commercecore.catalog.SkuSnapshot;
import br.com.commercecore.inventory.InventoryService;
import br.com.commercecore.inventory.ReceiveStockCommand;
import br.com.commercecore.inventory.StockOriginType;
import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.partner.PartnerRole;
import br.com.commercecore.partner.PartnerService;
import br.com.commercecore.purchasing.internal.PurchaseOrderEntity;
import br.com.commercecore.purchasing.internal.PurchaseOrderItemEntity;
import br.com.commercecore.purchasing.internal.PurchaseOrderRepository;
import br.com.commercecore.purchasing.internal.PurchaseReceiptEntity;
import br.com.commercecore.purchasing.internal.PurchaseReceiptItemEntity;
import br.com.commercecore.purchasing.internal.PurchaseReceiptRepository;
import br.com.commercecore.purchasing.internal.PurchaseRequisitionEntity;
import br.com.commercecore.purchasing.internal.PurchaseRequisitionItemEntity;
import br.com.commercecore.purchasing.internal.PurchaseRequisitionRepository;
import br.com.commercecore.purchasing.internal.SupplierQuoteEntity;
import br.com.commercecore.purchasing.internal.SupplierQuoteItemEntity;
import br.com.commercecore.purchasing.internal.SupplierQuoteRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.ConflictException;
import br.com.commercecore.shared.CurrentActor;
import br.com.commercecore.shared.Dinheiro;
import br.com.commercecore.shared.NotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchasingService {

    private final PurchaseRequisitionRepository requisicoes;
    private final SupplierQuoteRepository cotacoes;
    private final PurchaseOrderRepository pedidos;
    private final PurchaseReceiptRepository recebimentos;
    private final CatalogService catalogo;
    private final PartnerService parceiros;
    private final InventoryService estoque;
    private final BranchAccessService acessoFilial;
    private final CurrentActor atorAtual;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio;

    public PurchasingService(
            PurchaseRequisitionRepository requisicoes,
            SupplierQuoteRepository cotacoes,
            PurchaseOrderRepository pedidos,
            PurchaseReceiptRepository recebimentos,
            CatalogService catalogo,
            PartnerService parceiros,
            InventoryService estoque,
            BranchAccessService acessoFilial,
            CurrentActor atorAtual,
            ApplicationEventPublisher eventos,
            Clock relogio) {
        this.requisicoes = requisicoes;
        this.cotacoes = cotacoes;
        this.pedidos = pedidos;
        this.recebimentos = recebimentos;
        this.catalogo = catalogo;
        this.parceiros = parceiros;
        this.estoque = estoque;
        this.acessoFilial = acessoFilial;
        this.atorAtual = atorAtual;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Transactional
    public PurchaseRequisitionResponse criarRequisicao(CreatePurchaseRequisitionRequest requisicao) {
        var filial = acessoFilial.garantirAcesso(requisicao.filialId());
        if (!filial.empresaId().equals(requisicao.empresaId())) {
            throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
        }
        garantirSkusUnicos(requisicao.itens().stream().map(PurchaseRequisitionItemRequest::skuId).toList());
        PurchaseRequisitionEntity entidade = new PurchaseRequisitionEntity(
                numero("RC", requisicoes.proximoNumero()), requisicao.empresaId(), requisicao.filialId(),
                requisicao.justificativa().trim(), atorAtual.id());
        for (PurchaseRequisitionItemRequest item : requisicao.itens()) {
            SkuSnapshot sku = catalogo.obterSku(item.skuId());
            garantirSkuDaEmpresa(sku, requisicao.empresaId());
            entidade.adicionarItem(new PurchaseRequisitionItemEntity(
                    item.skuId(), sku.codigo(), sku.nomeProduto(), validarQuantidade(item.quantidade(), sku)));
        }
        return resposta(requisicoes.save(entidade));
    }

    @Transactional
    public PurchaseRequisitionResponse aprovarRequisicao(UUID id) {
        PurchaseRequisitionEntity requisicao = obterRequisicao(id);
        acessoFilial.garantirAcesso(requisicao.getFilialId());
        requisicao.aprovar(atorAtual.id(), relogio.instant());
        return resposta(requisicao);
    }

    @Transactional
    public SupplierQuoteResponse registrarCotacao(UUID requisicaoId, CreateSupplierQuoteRequest requisicao) {
        PurchaseRequisitionEntity compra = obterRequisicao(requisicaoId);
        acessoFilial.garantirAcesso(compra.getFilialId());
        if (compra.getStatus() != RequisitionStatus.APROVADA) {
            throw new BusinessRuleException("REQUISICAO_NAO_APROVADA", "A requisicao precisa estar aprovada para receber cotacoes.");
        }
        var fornecedor = parceiros.obter(requisicao.fornecedorId(), compra.getEmpresaId(), PartnerRole.FORNECEDOR);
        garantirSkusUnicos(requisicao.itens().stream().map(SupplierQuoteItemRequest::skuId).toList());
        if (requisicao.itens().size() != compra.getItens().size()) {
            throw new BusinessRuleException("COTACAO_INCOMPLETA", "A cotacao deve conter todos os itens da requisicao.");
        }
        SupplierQuoteEntity cotacao = new SupplierQuoteEntity(
                numero("CT", cotacoes.proximoNumero()), compra.getId(), compra.getEmpresaId(), compra.getFilialId(),
                fornecedor.id(), fornecedor.nome(), requisicao.validoAte());
        for (SupplierQuoteItemRequest item : requisicao.itens()) {
            PurchaseRequisitionItemEntity solicitado = compra.getItens().stream()
                    .filter(candidato -> candidato.getSkuId().equals(item.skuId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "SKU_FORA_DA_REQUISICAO", "A cotacao possui um SKU nao solicitado."));
            if (solicitado.getQuantidade().compareTo(item.quantidade()) != 0) {
                throw new BusinessRuleException(
                        "QUANTIDADE_COTADA_DIVERGENTE", "A quantidade cotada deve ser igual a solicitada.");
            }
            cotacao.adicionarItem(new SupplierQuoteItemEntity(
                    item.skuId(), item.quantidade(), Dinheiro.de(item.custoUnitario())));
        }
        return resposta(cotacoes.save(cotacao));
    }

    @Transactional(readOnly = true)
    public List<SupplierQuoteResponse> compararCotacoes(UUID requisicaoId) {
        PurchaseRequisitionEntity requisicao = obterRequisicao(requisicaoId);
        acessoFilial.garantirAcesso(requisicao.getFilialId());
        return cotacoes.findByRequisicaoIdOrderByTotalAsc(requisicaoId).stream().map(PurchasingService::resposta).toList();
    }

    @Transactional
    public PurchaseOrderResponse gerarPedido(UUID cotacaoId, CreatePurchaseOrderRequest requisicao) {
        SupplierQuoteEntity cotacao = obterCotacao(cotacaoId);
        PurchaseRequisitionEntity compra = obterRequisicao(cotacao.getRequisicaoId());
        acessoFilial.garantirAcesso(compra.getFilialId());
        estoque.validarDeposito(compra.getEmpresaId(), compra.getFilialId(), requisicao.depositoId());
        if (requisicao.primeiroVencimento().isBefore(LocalDate.now(relogio))) {
            throw new BusinessRuleException("VENCIMENTO_INVALIDO", "O primeiro vencimento nao pode estar no passado.");
        }
        cotacao.selecionar(relogio.instant());
        compra.converter();
        PurchaseOrderEntity pedido = new PurchaseOrderEntity(
                numero("PC", pedidos.proximoNumero()), compra.getId(), cotacao.getId(), compra.getEmpresaId(),
                compra.getFilialId(), requisicao.depositoId(), cotacao.getFornecedorId(), cotacao.getFornecedorNome(),
                requisicao.numeroParcelas(), requisicao.primeiroVencimento(), relogio.instant());
        for (SupplierQuoteItemEntity item : cotacao.getItens()) {
            PurchaseRequisitionItemEntity solicitado = compra.getItens().stream()
                    .filter(candidato -> candidato.getSkuId().equals(item.getSkuId()))
                    .findFirst().orElseThrow();
            pedido.adicionarItem(new PurchaseOrderItemEntity(
                    item.getSkuId(), solicitado.getCodigoSku(), solicitado.getNomeProduto(), item.getQuantidade(),
                    item.getCustoUnitario()));
        }
        return resposta(pedidos.save(pedido));
    }

    @Transactional
    public PurchaseReceiptResponse receber(
            UUID pedidoId, String chaveIdempotencia, ReceivePurchaseRequest requisicao) {
        validarChaveIdempotencia(chaveIdempotencia);
        var repetido = recebimentos.findByChaveIdempotencia(chaveIdempotencia);
        if (repetido.isPresent()) {
            PurchaseReceiptEntity recebimento = repetido.get();
            if (!recebimento.getPedidoId().equals(pedidoId)
                    || !recebimento.getDocumentoFornecedor().equals(requisicao.documentoFornecedor().trim())) {
                throw new ConflictException(
                        "IDEMPOTENCIA_DIVERGENTE", "A chave ja foi usada em outro recebimento.");
            }
            return resposta(recebimento);
        }
        PurchaseOrderEntity pedido = pedidos.buscarComBloqueio(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido de compra nao encontrado."));
        acessoFilial.garantirAcesso(pedido.getFilialId());
        if (pedido.getStatus() == PurchaseOrderStatus.RECEBIDO || pedido.getStatus() == PurchaseOrderStatus.CANCELADO) {
            throw new BusinessRuleException("STATUS_PEDIDO_COMPRA_INVALIDO", "O pedido nao aceita novos recebimentos.");
        }
        validarPartesUnicas(requisicao.itens());
        Instant recebidoEm = requisicao.recebidoEm() == null ? relogio.instant() : requisicao.recebidoEm();
        PurchaseReceiptEntity recebimento = new PurchaseReceiptEntity(
                numero("REC", recebimentos.proximoNumero()), pedido.getId(),
                requisicao.documentoFornecedor().trim(), recebidoEm, chaveIdempotencia.trim());
        for (ReceivePurchaseItemRequest item : requisicao.itens()) {
            PurchaseOrderItemEntity itemPedido = pedido.getItens().stream()
                    .filter(candidato -> candidato.getSkuId().equals(item.skuId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleException(
                            "SKU_FORA_DO_PEDIDO", "O recebimento possui um SKU que nao esta no pedido."));
            SkuSnapshot sku = catalogo.obterSku(item.skuId());
            BigDecimal quantidade = validarQuantidade(item.quantidade(), sku);
            itemPedido.receber(quantidade);
            recebimento.adicionarItem(new PurchaseReceiptItemEntity(
                    item.skuId(), quantidade, normalizar(item.lote()), item.validadeLote(), itemPedido.getCustoUnitario()));
            estoque.receber(new ReceiveStockCommand(
                    pedido.getEmpresaId(), pedido.getFilialId(), pedido.getDepositoId(), item.skuId(), quantidade,
                    item.lote(), item.validadeLote(), StockOriginType.PEDIDO_COMPRA, recebimento.getId()));
        }
        pedido.atualizarAposRecebimento(recebidoEm);
        recebimentos.save(recebimento);
        eventos.publishEvent(new PurchaseReceivedEvent(
                UUID.randomUUID(), recebimento.getId(), recebimento.getNumero(), pedido.getId(),
                pedido.getEmpresaId(), pedido.getFilialId(), pedido.getFornecedorId(), pedido.getFornecedorNome(),
                recebimento.getTotal().valor(), pedido.getNumeroParcelas(), pedido.getPrimeiroVencimento(), recebidoEm));
        return resposta(recebimento);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse obterPedidoResposta(UUID id) {
        PurchaseOrderEntity pedido = pedidos.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido de compra nao encontrado."));
        acessoFilial.garantirAcesso(pedido.getFilialId());
        return resposta(pedido);
    }

    private PurchaseRequisitionEntity obterRequisicao(UUID id) {
        return requisicoes.findById(id).orElseThrow(() -> new NotFoundException("Requisicao de compra nao encontrada."));
    }

    private SupplierQuoteEntity obterCotacao(UUID id) {
        return cotacoes.findById(id).orElseThrow(() -> new NotFoundException("Cotacao de fornecedor nao encontrada."));
    }

    private BigDecimal validarQuantidade(BigDecimal quantidade, SkuSnapshot sku) {
        try {
            BigDecimal normalizada = quantidade.setScale(3, RoundingMode.UNNECESSARY);
            if (normalizada.signum() <= 0 || (!sku.aceitaFracionado() && normalizada.stripTrailingZeros().scale() > 0)) {
                throw new ArithmeticException();
            }
            return normalizada;
        } catch (ArithmeticException excecao) {
            throw new BusinessRuleException(
                    "QUANTIDADE_INVALIDA", "A quantidade deve ser positiva, respeitar a unidade e ter ate tres casas.");
        }
    }

    private void garantirSkuDaEmpresa(SkuSnapshot sku, UUID empresaId) {
        if (!sku.empresaId().equals(empresaId)) {
            throw new BusinessRuleException("SKU_DE_OUTRA_EMPRESA", "O SKU nao pertence a empresa informada.");
        }
    }

    private void garantirSkusUnicos(List<UUID> ids) {
        if (ids.stream().distinct().count() != ids.size()) {
            throw new BusinessRuleException("SKU_REPETIDO", "Cada SKU deve aparecer somente uma vez.");
        }
    }

    private void validarPartesUnicas(List<ReceivePurchaseItemRequest> itens) {
        Set<String> chaves = new HashSet<>();
        for (ReceivePurchaseItemRequest item : itens) {
            String chave = item.skuId() + ":" + normalizar(item.lote());
            if (!chaves.add(chave)) {
                throw new BusinessRuleException("ITEM_RECEBIMENTO_REPETIDO", "O mesmo SKU e lote foi informado mais de uma vez.");
            }
        }
    }

    private static PurchaseRequisitionResponse resposta(PurchaseRequisitionEntity requisicao) {
        return new PurchaseRequisitionResponse(
                requisicao.getId(), requisicao.getNumero(), requisicao.getEmpresaId(), requisicao.getFilialId(),
                requisicao.getJustificativa(), requisicao.getSolicitadaPor(), requisicao.getAprovadaPor(),
                requisicao.getAprovadaEm(), requisicao.getStatus(), requisicao.getItens().stream()
                        .map(item -> new PurchaseRequisitionItemResponse(
                                item.getSkuId(), item.getCodigoSku(), item.getNomeProduto(), item.getQuantidade()))
                        .toList(), requisicao.getVersao());
    }

    private static SupplierQuoteResponse resposta(SupplierQuoteEntity cotacao) {
        return new SupplierQuoteResponse(
                cotacao.getId(), cotacao.getNumero(), cotacao.getRequisicaoId(), cotacao.getFornecedorId(),
                cotacao.getFornecedorNome(), cotacao.getValidoAte(), cotacao.getStatus(), cotacao.getTotal(),
                cotacao.getItens().stream().map(item -> new SupplierQuoteItemResponse(
                        item.getSkuId(), item.getQuantidade(), item.getCustoUnitario(), item.getSubtotal())).toList(),
                cotacao.getVersao());
    }

    private static PurchaseOrderResponse resposta(PurchaseOrderEntity pedido) {
        return new PurchaseOrderResponse(
                pedido.getId(), pedido.getNumero(), pedido.getRequisicaoId(), pedido.getCotacaoId(),
                pedido.getEmpresaId(), pedido.getFilialId(), pedido.getDepositoId(), pedido.getFornecedorId(),
                pedido.getFornecedorNome(), pedido.getStatus(), pedido.getTotal(), pedido.getNumeroParcelas(),
                pedido.getPrimeiroVencimento(), pedido.getEmitidoEm(), pedido.getConcluidoEm(),
                pedido.getItens().stream().map(item -> new PurchaseOrderItemResponse(
                        item.getSkuId(), item.getCodigoSku(), item.getNomeProduto(), item.getQuantidadePedida(),
                        item.getQuantidadeRecebida(), item.quantidadePendente(), item.getCustoUnitario(),
                        item.getSubtotal())).toList(), pedido.getVersao());
    }

    private static PurchaseReceiptResponse resposta(PurchaseReceiptEntity recebimento) {
        return new PurchaseReceiptResponse(
                recebimento.getId(), recebimento.getNumero(), recebimento.getPedidoId(),
                recebimento.getDocumentoFornecedor(), recebimento.getTotal(), recebimento.getRecebidoEm(),
                recebimento.getChaveIdempotencia());
    }

    private static String numero(String prefixo, long sequencia) {
        return "%s-%08d".formatted(prefixo, sequencia);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }

    private static void validarChaveIdempotencia(String chave) {
        if (chave == null || chave.isBlank() || chave.length() > 100) {
            throw new BusinessRuleException(
                    "CHAVE_IDEMPOTENCIA_INVALIDA", "O cabecalho Idempotency-Key e obrigatorio e aceita ate 100 caracteres.");
        }
    }
}
