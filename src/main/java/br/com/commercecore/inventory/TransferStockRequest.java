package br.com.commercecore.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record TransferStockRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotNull UUID depositoOrigemId,
        @NotNull UUID depositoDestinoId,
        @NotNull UUID skuId,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidade,
        @Size(max = 60) String lote,
        @NotBlank @Size(max = 300) String justificativa
) {
}
