package br.com.commercecore.catalog;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID empresaId,
        UUID categoriaId,
        String codigo,
        String nome,
        String descricao,
        boolean ativo,
        long versao,
        List<SkuResponse> skus
) {
}
