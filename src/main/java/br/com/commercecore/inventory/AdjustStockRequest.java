package br.com.commercecore.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AdjustStockRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotNull UUID depositoId,
        @NotNull UUID skuId,
        @NotNull AdjustmentDirection direcao,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade,
        @Size(max = 60) String lote,
        LocalDate validadeLote,
        @NotBlank @Size(max = 300) String justificativa
) {
}
