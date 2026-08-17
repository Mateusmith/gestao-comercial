package br.com.commercecore.sales.internal;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    Optional<InvoiceEntity> findByPedidoId(UUID pedidoId);

    @Query(value = "select nextval('fatura_venda_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
