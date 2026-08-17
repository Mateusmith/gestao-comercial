package br.com.commercecore.finance;

import br.com.commercecore.shared.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/financeiro")
@Tag(name = "Financeiro")
public class FinanceController {

    private final FinanceService financeiro;

    public FinanceController(FinanceService financeiro) {
        this.financeiro = financeiro;
    }

    @PostMapping("/contas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO')")
    @Operation(summary = "Cria uma conta de caixa ou banco por filial")
    ResponseEntity<FinancialAccountResponse> criarConta(
            @Valid @RequestBody CreateFinancialAccountRequest requisicao) {
        FinancialAccountResponse resposta = financeiro.criarConta(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/financeiro/contas/" + resposta.id())).body(resposta);
    }

    @GetMapping("/titulos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO', 'FINANCEIRO', 'AUDITOR')")
    @Operation(summary = "Lista contas a receber ou pagar por status")
    PageResponse<FinancialTitleResponse> listarTitulos(
            @RequestParam UUID empresaId,
            @RequestParam TitleType tipo,
            @RequestParam(required = false) List<FinancialTitleStatus> status,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return financeiro.listarTitulos(empresaId, tipo, status, pagina, tamanho);
    }

    @GetMapping("/titulos/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO', 'FINANCEIRO', 'AUDITOR')")
    FinancialTitleResponse obterTitulo(@PathVariable UUID id) {
        return financeiro.obterTitulo(id);
    }

    @PostMapping("/titulos/{id}/liquidacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO', 'FINANCEIRO')")
    @Operation(summary = "Recebe ou paga um titulo com protecao idempotente")
    SettlementResponse liquidar(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody SettleTitleRequest requisicao) {
        return financeiro.liquidar(id, chaveIdempotencia, requisicao);
    }

    @PostMapping("/liquidacoes/{id}/estornos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO')")
    @Operation(summary = "Estorna uma liquidacao sem apagar o lancamento original")
    SettlementResponse estornar(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody ReverseSettlementRequest requisicao) {
        return financeiro.estornar(id, chaveIdempotencia, requisicao);
    }
}
