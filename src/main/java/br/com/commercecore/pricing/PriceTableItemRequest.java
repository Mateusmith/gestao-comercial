package br.com.commercecore.pricing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PriceTableItemRequest(
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorVenda,
        @NotNull @DecimalMin(value = "0.00") BigDecimal custoReferencia
) {
}
