package br.com.commercecore.pricing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/precificacao")
@Tag(name = "Precificacao")
public class PricingController {

    private final PricingService precificacao;

    public PricingController(PricingService precificacao) {
        this.precificacao = precificacao;
    }

    @PostMapping("/tabelas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL')")
    @Operation(summary = "Cria uma tabela de precos com vigencia e escopo opcional por filial")
    ResponseEntity<PriceTableResponse> criarTabela(@Valid @RequestBody CreatePriceTableRequest requisicao) {
        PriceTableResponse resposta = precificacao.criarTabela(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/precificacao/tabelas/" + resposta.id())).body(resposta);
    }

    @PostMapping("/promocoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL')")
    @Operation(summary = "Cria uma promocao por SKU, periodo, quantidade e cupom opcional")
    ResponseEntity<PromotionResponse> criarPromocao(@Valid @RequestBody CreatePromotionRequest requisicao) {
        PromotionResponse resposta = precificacao.criarPromocao(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/precificacao/promocoes/" + resposta.id())).body(resposta);
    }

    @PostMapping("/simulacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'VENDEDOR')")
    @Operation(summary = "Explica o preco aplicado e calcula desconto, subtotal e margem")
    PriceCalculation simular(@Valid @RequestBody PriceSimulationRequest requisicao) {
        return precificacao.calcular(requisicao);
    }
}
