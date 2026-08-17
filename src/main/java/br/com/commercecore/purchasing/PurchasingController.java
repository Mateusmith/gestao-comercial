package br.com.commercecore.purchasing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/compras")
@Tag(name = "Compras")
public class PurchasingController {

    private final PurchasingService compras;

    public PurchasingController(PurchasingService compras) {
        this.compras = compras;
    }

    @PostMapping("/requisicoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'COMPRADOR')")
    @Operation(summary = "Cria uma requisicao de compra justificada")
    ResponseEntity<PurchaseRequisitionResponse> criarRequisicao(
            @Valid @RequestBody CreatePurchaseRequisitionRequest requisicao) {
        PurchaseRequisitionResponse resposta = compras.criarRequisicao(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/compras/requisicoes/" + resposta.id())).body(resposta);
    }

    @PostMapping("/requisicoes/{id}/aprovacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS')")
    @Operation(summary = "Aprova uma requisicao com autoria e data")
    PurchaseRequisitionResponse aprovar(@PathVariable UUID id) {
        return compras.aprovarRequisicao(id);
    }

    @PostMapping("/requisicoes/{id}/cotacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'COMPRADOR')")
    @Operation(summary = "Registra uma proposta de fornecedor")
    ResponseEntity<SupplierQuoteResponse> registrarCotacao(
            @PathVariable UUID id, @Valid @RequestBody CreateSupplierQuoteRequest requisicao) {
        SupplierQuoteResponse resposta = compras.registrarCotacao(id, requisicao);
        return ResponseEntity.created(URI.create("/api/v1/compras/cotacoes/" + resposta.id())).body(resposta);
    }

    @GetMapping("/requisicoes/{id}/comparativo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'COMPRADOR', 'AUDITOR')")
    @Operation(summary = "Compara propostas pelo valor total")
    List<SupplierQuoteResponse> comparar(@PathVariable UUID id) {
        return compras.compararCotacoes(id);
    }

    @PostMapping("/cotacoes/{id}/pedido")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'COMPRADOR')")
    @Operation(summary = "Seleciona a cotacao e emite o pedido de compra")
    ResponseEntity<PurchaseOrderResponse> gerarPedido(
            @PathVariable UUID id, @Valid @RequestBody CreatePurchaseOrderRequest requisicao) {
        PurchaseOrderResponse resposta = compras.gerarPedido(id, requisicao);
        return ResponseEntity.created(URI.create("/api/v1/compras/pedidos/" + resposta.id())).body(resposta);
    }

    @GetMapping("/pedidos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'COMPRADOR', 'ESTOQUISTA', 'FINANCEIRO', 'AUDITOR')")
    PurchaseOrderResponse obterPedido(@PathVariable UUID id) {
        return compras.obterPedidoResposta(id);
    }

    @PostMapping("/pedidos/{id}/recebimentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMPRAS', 'ESTOQUISTA')")
    @Operation(summary = "Recebe itens parcial ou totalmente, atualiza estoque e gera contas a pagar")
    PurchaseReceiptResponse receber(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody ReceivePurchaseRequest requisicao) {
        return compras.receber(id, chaveIdempotencia, requisicao);
    }
}
