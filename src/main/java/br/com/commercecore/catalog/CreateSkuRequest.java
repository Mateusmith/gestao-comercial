package br.com.commercecore.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateSkuRequest(
        @NotBlank @Size(max = 50) String codigo,
        @Size(max = 50) String codigoBarras,
        @Size(max = 120) String descricaoVariacao,
        @NotNull MeasurementUnit unidadeMedida,
        boolean controlaLote,
        boolean aceitaFracionado,
        @NotNull @DecimalMin(value = "0.000") BigDecimal estoqueMinimo
) {
}
