package br.com.commercecore.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReserveStockCommand(
        UUID empresaId,
        UUID filialId,
        UUID depositoId,
        UUID skuId,
        BigDecimal quantidade,
        StockOriginType tipoOrigem,
        UUID origemId,
        Instant expiraEm
) {
}
