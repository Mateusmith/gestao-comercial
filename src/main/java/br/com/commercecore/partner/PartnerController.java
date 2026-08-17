package br.com.commercecore.partner;

import br.com.commercecore.shared.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parceiros")
public class PartnerController {

    private final PartnerService servico;

    public PartnerController(PartnerService servico) {
        this.servico = servico;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE_COMERCIAL','GERENTE_COMPRAS','VENDEDOR','COMPRADOR')")
    ResponseEntity<PartnerResponse> criar(@Valid @RequestBody CreatePartnerRequest requisicao) {
        PartnerResponse resposta = servico.criar(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/parceiros/" + resposta.id())).body(resposta);
    }

    @GetMapping
    PageResponse<PartnerResponse> listar(
            @RequestParam UUID empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return servico.listar(empresaId, pagina, tamanho);
    }
}
