package br.com.commercecore.catalog;

import java.util.UUID;

public record CategoryResponse(UUID id, UUID empresaId, String nome, boolean ativa) {
}
