package br.com.commercecore.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID depositoId,
        UUID skuId,
        String lote,
        MovementType tipo,
        BigDecimal quantidade,
        BigDecimal saldoFisicoAnterior,
        BigDecimal saldoFisicoPosterior,
        BigDecimal saldoReservadoAnterior,
        BigDecimal saldoReservadoPosterior,
        StockOriginType tipoOrigem,
        UUID origemId,
        String justificativa,
        String realizadoPor,
        Instant ocorridoEm
) {
}
