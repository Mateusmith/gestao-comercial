package br.com.commercecore.catalog.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    boolean existsByEmpresaIdAndNomeIgnoreCase(UUID empresaId, String nome);
}
