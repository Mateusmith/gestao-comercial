package br.com.commercecore.platform.internal;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<AuditEntity, UUID> {
    Page<AuditEntity> findByEmpresaIdAndOcorridoEmBetween(
            UUID empresaId, Instant inicio, Instant fim, Pageable pageable);
}
