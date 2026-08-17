package br.com.commercecore.purchasing.internal;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PurchaseOrderEntity p where p.id = :id")
    Optional<PurchaseOrderEntity> buscarComBloqueio(UUID id);

    @Query(value = "select nextval('pedido_compra_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
