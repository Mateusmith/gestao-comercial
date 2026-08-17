package br.com.commercecore.platform;

import br.com.commercecore.platform.internal.AuditEntity;
import br.com.commercecore.platform.internal.AuditRepository;
import br.com.commercecore.shared.PageResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditRepository auditorias;

    public AuditService(AuditRepository auditorias) {
        this.auditorias = auditorias;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(AuditRecordedEvent evento) {
        auditorias.save(new AuditEntity(evento));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditResponse> consultar(
            UUID empresaId, Instant inicio, Instant fim, int pagina, int tamanho) {
        var paginacao = PageRequest.of(
                pagina, Math.min(tamanho, 100), Sort.by(Sort.Direction.DESC, "ocorridoEm"));
        return PageResponse.de(auditorias.findByEmpresaIdAndOcorridoEmBetween(empresaId, inicio, fim, paginacao)
                .map(AuditService::resposta));
    }

    private static AuditResponse resposta(AuditEntity auditoria) {
        return new AuditResponse(
                auditoria.getId(), auditoria.getEmpresaId(), auditoria.getAtorId(), auditoria.getMetodoHttp(),
                auditoria.getCaminho(), auditoria.getParametros(), auditoria.getStatusHttp(),
                auditoria.getEnderecoIp(), auditoria.getCorrelacao(), auditoria.getOcorridoEm());
    }
}
