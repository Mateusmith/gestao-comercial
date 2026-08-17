package br.com.commercecore.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCategoryRequest(@NotNull UUID empresaId, @NotBlank @Size(max = 100) String nome) {
}
