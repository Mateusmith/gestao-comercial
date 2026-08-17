package br.com.commercecore.organization.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity, UUID> {
    boolean existsByCnpj(String cnpj);
}
