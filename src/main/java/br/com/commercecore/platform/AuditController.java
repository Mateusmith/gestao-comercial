package br.com.commercecore.platform;

import br.com.commercecore.shared.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria")
public class AuditController {

    private final AuditService auditoria;

    public AuditController(AuditService auditoria) {
        this.auditoria = auditoria;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'AUDITOR')")
    PageResponse<AuditResponse> consultar(
            @RequestParam UUID empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return auditoria.consultar(empresaId, inicio, fim, pagina, tamanho);
    }
}
