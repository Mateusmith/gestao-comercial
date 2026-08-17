package br.com.commercecore.sales;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemRequest(
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade
) {
}
