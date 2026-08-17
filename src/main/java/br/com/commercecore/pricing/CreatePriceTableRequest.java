package br.com.commercecore.pricing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreatePriceTableRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotBlank @Size(max = 100) String nome,
        @NotNull Instant vigenteDe,
        Instant vigenteAte,
        @NotEmpty List<@Valid PriceTableItemRequest> itens
) {
}
