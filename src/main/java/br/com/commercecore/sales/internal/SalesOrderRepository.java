package br.com.commercecore.sales.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalesOrderRepository extends JpaRepository<SalesOrderEntity, UUID> {
    @Query(value = "select nextval('pedido_venda_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
