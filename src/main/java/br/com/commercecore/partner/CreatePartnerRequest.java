package br.com.commercecore.partner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreatePartnerRequest(
        @NotNull UUID empresaId,
        @NotNull PersonType tipoPessoa,
        @NotBlank @Size(max = 160) String nomeRazaoSocial,
        @Size(max = 120) String nomeFantasia,
        @NotBlank String cpfCnpj,
        @Email @Size(max = 160) String email,
        @Size(max = 30) String telefone,
        @NotEmpty Set<PartnerRole> papeis
) {
}
