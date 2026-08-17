package br.com.commercecore.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseSettlementRequest(
        @NotBlank @Size(max = 300) String motivo
) {
}
