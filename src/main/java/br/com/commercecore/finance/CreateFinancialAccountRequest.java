package br.com.commercecore.finance;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateFinancialAccountRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotNull @DecimalMin(value = "0.00") BigDecimal saldoInicial
) {
}
