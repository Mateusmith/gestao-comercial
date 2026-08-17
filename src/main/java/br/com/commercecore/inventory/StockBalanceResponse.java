package br.com.commercecore.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StockBalanceResponse(
        UUID depositoId,
        UUID skuId,
        String lote,
        LocalDate validadeLote,
        BigDecimal saldoFisico,
        BigDecimal saldoReservado,
        BigDecimal saldoDisponivel
) {
}
