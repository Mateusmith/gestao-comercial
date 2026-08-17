package br.com.commercecore.inventory;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        UUID empresaId,
        UUID filialId,
        String codigo,
        String nome,
        boolean ativo
) {
}
