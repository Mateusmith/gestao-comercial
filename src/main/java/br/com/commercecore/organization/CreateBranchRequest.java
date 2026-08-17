package br.com.commercecore.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotBlank String cnpj,
        @NotBlank String fusoHorario
) {
}
