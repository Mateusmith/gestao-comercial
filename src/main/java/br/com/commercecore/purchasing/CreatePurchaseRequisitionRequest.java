package br.com.commercecore.purchasing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequisitionRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotBlank @Size(max = 300) String justificativa,
        @NotEmpty List<@Valid PurchaseRequisitionItemRequest> itens
) {
}
