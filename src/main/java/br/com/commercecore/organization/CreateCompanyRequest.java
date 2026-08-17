package br.com.commercecore.organization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCompanyRequest(
        @NotBlank @Size(max = 160) String razaoSocial,
        @NotBlank @Size(max = 120) String nomeFantasia,
        @NotBlank String cnpj
) {
}
