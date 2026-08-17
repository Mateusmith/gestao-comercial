package br.com.commercecore.purchasing.internal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceiptEntity, UUID> {
    Optional<PurchaseReceiptEntity> findByChaveIdempotencia(String chaveIdempotencia);

    @Query(value = "select nextval('recebimento_compra_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
