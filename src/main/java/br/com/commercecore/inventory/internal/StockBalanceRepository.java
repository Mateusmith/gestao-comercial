package br.com.commercecore.inventory.internal;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockBalanceRepository extends JpaRepository<StockBalanceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<StockBalanceEntity> findByDepositoIdAndSkuIdAndLote(UUID depositoId, UUID skuId, String lote);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from StockBalanceEntity s
            where s.depositoId = :depositoId
              and s.skuId = :skuId
              and s.saldoFisico > s.saldoReservado
            order by case when s.validadeLote is null then 1 else 0 end, s.validadeLote, s.criadoEm
            """)
    List<StockBalanceEntity> buscarDisponiveisFefo(
            @Param("depositoId") UUID depositoId,
            @Param("skuId") UUID skuId);

    List<StockBalanceEntity> findByDepositoIdAndSkuIdOrderByValidadeLoteAsc(UUID depositoId, UUID skuId);
}
