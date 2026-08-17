package br.com.commercecore.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateWarehouseRequest(
        @NotNull UUID empresaId,
        @NotNull UUID filialId,
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nome
) {
}
