package br.com.commercecore.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettleTitleRequest(
        @NotNull UUID contaFinanceiraId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
        @NotNull PaymentMethod formaPagamento,
        Instant ocorridoEm,
        @Size(max = 300) String observacao
) {
}
