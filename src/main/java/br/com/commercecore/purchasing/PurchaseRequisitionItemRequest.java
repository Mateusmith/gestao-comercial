package br.com.commercecore.purchasing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseRequisitionItemRequest(
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade
) {
}
