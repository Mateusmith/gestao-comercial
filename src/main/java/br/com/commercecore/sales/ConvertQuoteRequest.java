package br.com.commercecore.sales;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ConvertQuoteRequest(
        @NotNull UUID depositoId,
        @Min(1) @Max(24) int numeroParcelas,
        @NotNull @FutureOrPresent LocalDate primeiroVencimento
) {
}
