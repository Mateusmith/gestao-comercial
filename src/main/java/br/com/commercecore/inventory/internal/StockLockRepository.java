package br.com.commercecore.inventory.internal;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface StockLockRepository extends Repository<StockBalanceEntity, java.util.UUID> {

    @Query(value = "select pg_advisory_xact_lock(hashtextextended(:chave, 0))::text", nativeQuery = true)
    String travar(@Param("chave") String chave);
}
