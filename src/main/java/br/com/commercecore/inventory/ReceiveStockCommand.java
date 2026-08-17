package br.com.commercecore.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiveStockCommand(
        UUID empresaId,
        UUID filialId,
        UUID depositoId,
        UUID skuId,
        BigDecimal quantidade,
        String lote,
        LocalDate validadeLote,
        StockOriginType tipoOrigem,
        UUID origemId
) {
}
