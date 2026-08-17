package br.com.commercecore.sales;

import br.com.commercecore.catalog.CatalogService;
import br.com.commercecore.catalog.SkuSnapshot;
import br.com.commercecore.inventory.InventoryService;
import br.com.commercecore.inventory.ReserveStockCommand;
import br.com.commercecore.inventory.StockOriginType;
import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.partner.PartnerRole;
import br.com.commercecore.partner.PartnerService;
import br.com.commercecore.pricing.PriceCalculation;
import br.com.commercecore.pricing.PriceSimulationRequest;
import br.com.commercecore.pricing.PricingService;
import br.com.commercecore.sales.internal.InvoiceEntity;
import br.com.commercecore.sales.internal.InvoiceRepository;
import br.com.commercecore.sales.internal.QuoteEntity;
import br.com.commercecore.sales.internal.QuoteItemEntity;
import br.com.commercecore.sales.internal.QuoteRepository;
import br.com.commercecore.sales.internal.SalesOrderEntity;
import br.com.commercecore.sales.internal.SalesOrderItemEntity;
import br.com.commercecore.sales.internal.SalesOrderRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.NotFoundException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesService {

    private static final Duration DURACAO_RESERVA = Duration.ofHours(24);

    private final QuoteRepository orcamentos;
    private final SalesOrderRepository pedidos;
    private final InvoiceRepository faturas;
    private final PartnerService parceiros;
    private final CatalogService catalogo;
    private final PricingService precificacao;
    private final InventoryService estoque;
    private final BranchAccessService acessoFilial;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio;

    public SalesService(
            QuoteRepository orcamentos,
            SalesOrderRepository pedidos,
            InvoiceRepository faturas,
            PartnerService parceiros,
            CatalogService catalogo,
            PricingService precificacao,
            InventoryService estoque,
            BranchAccessService acessoFilial,
            ApplicationEventPublisher eventos,
            Clock relogio) {
        this.orcamentos = orcamentos;
        this.pedidos = pedidos;
        this.faturas = faturas;
        this.parceiros = parceiros;
        this.catalogo = catalogo;
        this.precificacao = precificacao;
        this.estoque = estoque;
        this.acessoFilial = acessoFilial;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Transactional
    public QuoteResponse criarOrcamento(CreateQuoteRequest requisicao) {
        var filial = acessoFilial.garantirAcesso(requisicao.filialId());
        if (!filial.empresaId().equals(requisicao.empresaId())) {
            throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
        }
        var cliente = parceiros.obter(requisicao.clienteId(), requisicao.empresaId(), PartnerRole.CLIENTE);
        var ids = requisicao.itens().stream().map(QuoteItemRequest::skuId).toList();
        if (ids.stream().distinct().count() != ids.size()) {
            throw new BusinessRuleException("SKU_REPETIDO", "Cada SKU deve aparecer somente uma vez no orcamento.");
        }

        QuoteEntity orcamento = new QuoteEntity(
                numero("ORC", orcamentos.proximoNumero()), requisicao.empresaId(), requisicao.filialId(),
                cliente.id(), cliente.nome(), requisicao.validoAte(), normalizarCupom(requisicao.codigoCupom()));

        for (QuoteItemRequest item : requisicao.itens()) {
            SkuSnapshot sku = catalogo.obterSku(item.skuId());
            PriceCalculation calculo = precificacao.calcular(new PriceSimulationRequest(
                    requisicao.empresaId(), requisicao.filialId(), item.skuId(), item.quantidade(),
                    requisicao.codigoCupom(), relogio.instant()));
            orcamento.adicionarItem(new QuoteItemEntity(
                    item.skuId(), sku.codigo(), sku.nomeProduto(), calculo.quantidade(), calculo.precoUnitarioBase(),
                    calculo.descontoUnitario(), calculo.precoUnitarioFinal(), calculo.subtotal(),
                    calculo.tabelaPrecoId(), calculo.promocaoId()));
        }
        return resposta(orcamentos.save(orcamento));
    }

    @Transactional
    public QuoteResponse enviarOrcamento(UUID id) {
        QuoteEntity orcamento = obterOrcamento(id);
        acessoFilial.garantirAcesso(orcamento.getFilialId());
        orcamento.enviar();
        return resposta(orcamento);
    }

    @Transactional
    public QuoteResponse aceitarOrcamento(UUID id) {
        QuoteEntity orcamento = obterOrcamento(id);
        acessoFilial.garantirAcesso(orcamento.getFilialId());
        orcamento.aceitar(relogio.instant());
        return resposta(orcamento);
    }

    @Transactional
    public SalesOrderResponse converterOrcamento(UUID id, ConvertQuoteRequest requisicao) {
        QuoteEntity orcamento = obterOrcamento(id);
        acessoFilial.garantirAcesso(orcamento.getFilialId());
        estoque.validarDeposito(orcamento.getEmpresaId(), orcamento.getFilialId(), requisicao.depositoId());
        if (requisicao.primeiroVencimento().isBefore(LocalDate.now(relogio))) {
            throw new BusinessRuleException(
                    "VENCIMENTO_INVALIDO", "O primeiro vencimento nao pode estar no passado.");
        }
        orcamento.converter();
        SalesOrderEntity pedido = new SalesOrderEntity(
                numero("PV", pedidos.proximoNumero()), orcamento.getId(), orcamento.getEmpresaId(),
                orcamento.getFilialId(), requisicao.depositoId(), orcamento.getClienteId(),
                orcamento.getClienteNome(), requisicao.numeroParcelas(), requisicao.primeiroVencimento());
        orcamento.getItens().forEach(item -> pedido.adicionarItem(new SalesOrderItemEntity(
                item.getSkuId(), item.getCodigoSku(), item.getNomeProduto(), item.getQuantidade(),
                item.getPrecoUnitarioFinal(), item.getDescontoUnitario(), item.getSubtotal())));
        return resposta(pedidos.save(pedido));
    }

    @Transactional
    public SalesOrderResponse confirmarPedido(UUID id) {
        SalesOrderEntity pedido = obterPedido(id);
        acessoFilial.garantirAcesso(pedido.getFilialId());
        if (pedido.getStatus() != SalesOrderStatus.RASCUNHO) {
            throw new BusinessRuleException("STATUS_PEDIDO_INVALIDO", "Somente um pedido em rascunho pode ser confirmado.");
        }
        Instant expiracao = relogio.instant().plus(DURACAO_RESERVA);
        pedido.getItens().forEach(item -> estoque.reservar(new ReserveStockCommand(
                pedido.getEmpresaId(), pedido.getFilialId(), pedido.getDepositoId(), item.getSkuId(),
                item.getQuantidade(), StockOriginType.PEDIDO_VENDA, pedido.getId(), expiracao)));
        pedido.confirmar(relogio.instant());
        return resposta(pedido);
    }

    @Transactional
    public InvoiceResponse faturarPedido(UUID id) {
        SalesOrderEntity pedido = obterPedido(id);
        acessoFilial.garantirAcesso(pedido.getFilialId());
        if (pedido.getStatus() == SalesOrderStatus.FATURADO) {
            return resposta(faturas.findByPedidoId(id).orElseThrow());
        }
        if (pedido.getStatus() != SalesOrderStatus.CONFIRMADO) {
            throw new BusinessRuleException("STATUS_PEDIDO_INVALIDO", "Somente um pedido confirmado pode ser faturado.");
        }
        estoque.consumirReservas(StockOriginType.PEDIDO_VENDA, pedido.getId());
        Instant agora = relogio.instant();
        InvoiceEntity fatura = faturas.save(new InvoiceEntity(
                numero("FAT", faturas.proximoNumero()), pedido.getId(), pedido.getEmpresaId(),
                pedido.getFilialId(), pedido.getTotal(), agora));
        pedido.faturar(agora);
        eventos.publishEvent(new SaleInvoicedEvent(
                UUID.randomUUID(), fatura.getId(), fatura.getNumero(), pedido.getId(), pedido.getEmpresaId(),
                pedido.getFilialId(), pedido.getClienteId(), pedido.getClienteNome(), pedido.getTotal().valor(),
                pedido.getNumeroParcelas(), pedido.getPrimeiroVencimento(), agora));
        return resposta(fatura);
    }

    @Transactional
    public SalesOrderResponse cancelarPedido(UUID id) {
        SalesOrderEntity pedido = obterPedido(id);
        acessoFilial.garantirAcesso(pedido.getFilialId());
        if (pedido.getStatus() == SalesOrderStatus.CONFIRMADO) {
            estoque.liberarReservas(StockOriginType.PEDIDO_VENDA, pedido.getId());
        }
        pedido.cancelar(relogio.instant());
        return resposta(pedido);
    }

    @Transactional(readOnly = true)
    public QuoteResponse obterOrcamentoResposta(UUID id) {
        QuoteEntity orcamento = obterOrcamento(id);
        acessoFilial.garantirAcesso(orcamento.getFilialId());
        return resposta(orcamento);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse obterPedidoResposta(UUID id) {
        SalesOrderEntity pedido = obterPedido(id);
        acessoFilial.garantirAcesso(pedido.getFilialId());
        return resposta(pedido);
    }

    private QuoteEntity obterOrcamento(UUID id) {
        return orcamentos.findById(id).orElseThrow(() -> new NotFoundException("Orcamento de venda nao encontrado."));
    }

    private SalesOrderEntity obterPedido(UUID id) {
        return pedidos.findById(id).orElseThrow(() -> new NotFoundException("Pedido de venda nao encontrado."));
    }

    private static QuoteResponse resposta(QuoteEntity orcamento) {
        return new QuoteResponse(
                orcamento.getId(), orcamento.getNumero(), orcamento.getEmpresaId(), orcamento.getFilialId(),
                orcamento.getClienteId(), orcamento.getClienteNome(), orcamento.getStatus(),
                orcamento.getValidoAte(), orcamento.getCodigoCupom(), orcamento.getTotal(),
                orcamento.getItens().stream().map(item -> new QuoteItemResponse(
                        item.getSkuId(), item.getCodigoSku(), item.getNomeProduto(), item.getQuantidade(),
                        item.getPrecoUnitarioBase(), item.getDescontoUnitario(), item.getPrecoUnitarioFinal(),
                        item.getSubtotal(), item.getTabelaPrecoId(), item.getPromocaoId())).toList(),
                orcamento.getVersao());
    }

    private static SalesOrderResponse resposta(SalesOrderEntity pedido) {
        return new SalesOrderResponse(
                pedido.getId(), pedido.getNumero(), pedido.getOrcamentoId(), pedido.getEmpresaId(),
                pedido.getFilialId(), pedido.getDepositoId(), pedido.getClienteId(), pedido.getClienteNome(),
                pedido.getStatus(), pedido.getTotal(), pedido.getNumeroParcelas(), pedido.getPrimeiroVencimento(),
                pedido.getConfirmadoEm(), pedido.getFaturadoEm(), pedido.getCanceladoEm(),
                pedido.getItens().stream().map(item -> new SalesOrderItemResponse(
                        item.getSkuId(), item.getCodigoSku(), item.getNomeProduto(), item.getQuantidade(),
                        item.getPrecoUnitario(), item.getDescontoUnitario(), item.getSubtotal())).toList(),
                pedido.getVersao());
    }

    private static InvoiceResponse resposta(InvoiceEntity fatura) {
        return new InvoiceResponse(
                fatura.getId(), fatura.getNumero(), fatura.getPedidoId(), fatura.getTotal(), fatura.getEmitidaEm());
    }

    private static String numero(String prefixo, long sequencia) {
        return "%s-%08d".formatted(prefixo, sequencia);
    }

    private static String normalizarCupom(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim().toUpperCase();
    }
}
