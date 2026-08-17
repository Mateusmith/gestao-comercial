package br.com.commercecore.sales;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vendas")
@Tag(name = "Vendas")
public class SalesController {

    private final SalesService vendas;

    public SalesController(SalesService vendas) {
        this.vendas = vendas;
    }

    @PostMapping("/orcamentos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Cria um orcamento com fotografia da precificacao")
    ResponseEntity<QuoteResponse> criarOrcamento(@Valid @RequestBody CreateQuoteRequest requisicao) {
        QuoteResponse resposta = vendas.criarOrcamento(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/vendas/orcamentos/" + resposta.id())).body(resposta);
    }

    @GetMapping("/orcamentos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    QuoteResponse obterOrcamento(@PathVariable UUID id) {
        return vendas.obterOrcamentoResposta(id);
    }

    @PostMapping("/orcamentos/{id}/envio")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Marca o orcamento como enviado")
    QuoteResponse enviar(@PathVariable UUID id) {
        return vendas.enviarOrcamento(id);
    }

    @PostMapping("/orcamentos/{id}/aceite")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Registra o aceite dentro da validade")
    QuoteResponse aceitar(@PathVariable UUID id) {
        return vendas.aceitarOrcamento(id);
    }

    @PostMapping("/orcamentos/{id}/conversao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Converte um orcamento aceito em pedido")
    ResponseEntity<SalesOrderResponse> converter(
            @PathVariable UUID id, @Valid @RequestBody ConvertQuoteRequest requisicao) {
        SalesOrderResponse resposta = vendas.converterOrcamento(id, requisicao);
        return ResponseEntity.created(URI.create("/api/v1/vendas/pedidos/" + resposta.id())).body(resposta);
    }

    @GetMapping("/pedidos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR', 'ESTOQUISTA', 'FINANCEIRO')")
    SalesOrderResponse obterPedido(@PathVariable UUID id) {
        return vendas.obterPedidoResposta(id);
    }

    @PostMapping("/pedidos/{id}/confirmacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Confirma o pedido e reserva estoque por FEFO")
    SalesOrderResponse confirmar(@PathVariable UUID id) {
        return vendas.confirmarPedido(id);
    }

    @PostMapping("/pedidos/{id}/faturamento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'FATURISTA')")
    @Operation(summary = "Fatura o pedido, baixa o estoque e publica o evento financeiro")
    InvoiceResponse faturar(@PathVariable UUID id) {
        return vendas.faturarPedido(id);
    }

    @PostMapping("/pedidos/{id}/cancelamento")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL')")
    @Operation(summary = "Cancela o pedido e libera reservas ativas")
    SalesOrderResponse cancelar(@PathVariable UUID id) {
        return vendas.cancelarPedido(id);
    }
}
