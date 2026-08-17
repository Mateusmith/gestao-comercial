package br.com.commercecore.organization.internal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {
    List<BranchEntity> findByEmpresaIdOrderByNome(UUID empresaId);
    boolean existsByCnpj(String cnpj);
}
