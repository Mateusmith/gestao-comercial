package br.com.commercecore.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResult(
        UUID origemId,
        UUID skuId,
        BigDecimal quantidadeTotal,
        Instant expiraEm,
        List<ReservationPart> partes
) {
    public record ReservationPart(UUID reservaId, String lote, BigDecimal quantidade) {
    }
}
