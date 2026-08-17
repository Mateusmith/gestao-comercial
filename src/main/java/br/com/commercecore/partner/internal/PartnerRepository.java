package br.com.commercecore.partner.internal;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<PartnerEntity, UUID> {
    boolean existsByEmpresaIdAndCpfCnpj(UUID empresaId, String cpfCnpj);
    Page<PartnerEntity> findByEmpresaId(UUID empresaId, Pageable paginacao);
}
