package br.com.commercecore.purchasing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReceivePurchaseItemRequest(
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade,
        @Size(max = 60) String lote,
        LocalDate validadeLote
) {
}
