package br.com.commercecore.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record SkuSnapshot(
        UUID id,
        UUID empresaId,
        String codigo,
        String nomeProduto,
        MeasurementUnit unidadeMedida,
        boolean controlaLote,
        boolean aceitaFracionado,
        BigDecimal estoqueMinimo,
        boolean ativo
) {
}
