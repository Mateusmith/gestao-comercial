package br.com.commercecore.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record SkuResponse(
        UUID id,
        String codigo,
        String codigoBarras,
        String descricaoVariacao,
        MeasurementUnit unidadeMedida,
        boolean controlaLote,
        boolean aceitaFracionado,
        BigDecimal estoqueMinimo,
        boolean ativo
) {
}
