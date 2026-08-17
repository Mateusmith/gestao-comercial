package br.com.commercecore.purchasing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public record ReceivePurchaseRequest(
        @NotBlank @Size(max = 50) String documentoFornecedor,
        Instant recebidoEm,
        @NotEmpty List<@Valid ReceivePurchaseItemRequest> itens
) {
}
