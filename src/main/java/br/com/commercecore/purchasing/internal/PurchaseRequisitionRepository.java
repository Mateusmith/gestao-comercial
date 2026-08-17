package br.com.commercecore.purchasing.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisitionEntity, UUID> {
    @Query(value = "select nextval('requisicao_compra_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
