package br.com.commercecore.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateProductRequest(
        @NotNull UUID empresaId,
        @NotNull UUID categoriaId,
        @NotBlank @Size(max = 40) String codigo,
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 500) String descricao,
        @NotEmpty List<@Valid CreateSkuRequest> skus
) {
}
