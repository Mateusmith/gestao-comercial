package br.com.commercecore.inventory.internal;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovementEntity, UUID> {
    Page<StockMovementEntity> findByDepositoIdAndSkuId(UUID depositoId, UUID skuId, Pageable pageable);
}
