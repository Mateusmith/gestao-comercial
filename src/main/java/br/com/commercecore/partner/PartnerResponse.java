package br.com.commercecore.partner;

import java.util.Set;
import java.util.UUID;

public record PartnerResponse(
        UUID id,
        UUID empresaId,
        PersonType tipoPessoa,
        String nomeRazaoSocial,
        String nomeFantasia,
        String cpfCnpj,
        String email,
        String telefone,
        Set<PartnerRole> papeis,
        boolean ativo,
        long versao
) {
}
