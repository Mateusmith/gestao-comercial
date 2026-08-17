package br.com.commercecore.purchasing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record SupplierQuoteItemRequest(
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade,
        @NotNull @DecimalMin(value = "0.01") BigDecimal custoUnitario
) {
}
