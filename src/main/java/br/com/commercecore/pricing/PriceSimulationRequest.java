package br.com.commercecore.pricing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceSimulationRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade,
        @Size(max = 40) String codigoCupom,
        Instant instanteReferencia
) {
}
