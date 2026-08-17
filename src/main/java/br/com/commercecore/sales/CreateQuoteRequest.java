package br.com.commercecore.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateQuoteRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotNull UUID clienteId,
        @NotNull @Future Instant validoAte,
        @Size(max = 40) String codigoCupom,
        @NotEmpty List<@Valid QuoteItemRequest> itens
) {
}
