package br.com.commercecore.inventory.internal;

import br.com.commercecore.inventory.ReservationStatus;
import br.com.commercecore.inventory.StockOriginType;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<StockReservationEntity> findByTipoOrigemAndOrigemIdAndStatusIn(
            StockOriginType tipoOrigem, UUID origemId, Collection<ReservationStatus> status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<StockReservationEntity> findTop100ByStatusAndExpiraEmBeforeOrderByExpiraEmAsc(
            ReservationStatus status, Instant instante);
}
