package br.com.commercecore.inventory.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {
    boolean existsByFilialIdAndCodigo(UUID filialId, String codigo);
}
