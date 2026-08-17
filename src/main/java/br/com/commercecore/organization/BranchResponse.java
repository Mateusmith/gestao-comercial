package br.com.commercecore.organization;

import java.util.UUID;

public record BranchResponse(UUID id, UUID empresaId, String codigo, String nome, String cnpj, String fusoHorario, boolean ativa, long versao) {
}
