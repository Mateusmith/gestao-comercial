package br.com.commercecore.catalog.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<SkuEntity, UUID> {
    boolean existsByCodigo(String codigo);
}
