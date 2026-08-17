package br.com.commercecore.catalog.internal;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    boolean existsByEmpresaIdAndCodigo(UUID empresaId, String codigo);
    Page<ProductEntity> findByEmpresaId(UUID empresaId, Pageable paginacao);
}
