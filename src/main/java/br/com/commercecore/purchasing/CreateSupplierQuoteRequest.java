package br.com.commercecore.purchasing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateSupplierQuoteRequest(
        @NotNull UUID fornecedorId,
        @NotNull @Future Instant validoAte,
        @NotEmpty List<@Valid SupplierQuoteItemRequest> itens
) {
}
