package br.com.commercecore.organization;

import java.util.UUID;

public record CompanyResponse(UUID id, String razaoSocial, String nomeFantasia, String cnpj, boolean ativa, long versao) {
}
