package br.com.commercecore.sales.internal;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuoteRepository extends JpaRepository<QuoteEntity, UUID> {
    @Query(value = "select nextval('orcamento_venda_numero_seq')", nativeQuery = true)
    Long proximoNumero();
}
