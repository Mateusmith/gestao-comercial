package br.com.commercecore.reporting;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatorios")
public class ReportingController {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final ReportingService relatorios;

    public ReportingController(ReportingService relatorios) {
        this.relatorios = relatorios;
    }

    @GetMapping("/resumo-gerencial")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_COMERCIAL', 'GERENTE_COMPRAS', 'GERENTE_FINANCEIRO', 'AUDITOR')")
    @Operation(summary = "Consolida vendas, compras, financeiro e reposicao")
    ManagementSummaryResponse resumo(
            @RequestParam UUID empresaId,
            @RequestParam UUID filialId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorios.resumo(empresaId, filialId, inicio, fim);
    }

    @GetMapping("/reposicao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE', 'GERENTE_COMPRAS', 'COMPRADOR', 'AUDITOR')")
    @Operation(summary = "Lista SKUs abaixo ou no estoque minimo")
    List<ReplenishmentItemResponse> reposicao(
            @RequestParam UUID empresaId, @RequestParam UUID filialId) {
        return relatorios.itensReposicao(empresaId, filialId);
    }

    @GetMapping("/inadimplencia")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_FINANCEIRO', 'FINANCEIRO', 'AUDITOR')")
    @Operation(summary = "Agrupa contas a receber por faixa de atraso")
    AgingResponse inadimplencia(
            @RequestParam UUID empresaId,
            @RequestParam UUID filialId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referencia) {
        return relatorios.inadimplencia(empresaId, filialId, referencia);
    }

    @GetMapping(value = "/exportacoes/gerencial.xlsx", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    @Operation(summary = "Exporta o pacote gerencial em XLSX")
    ResponseEntity<byte[]> exportar(
            @RequestParam UUID empresaId,
            @RequestParam UUID filialId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commercecore-gerencial.xlsx")
                .contentType(XLSX)
                .body(relatorios.exportar(empresaId, filialId, inicio, fim));
    }
}
