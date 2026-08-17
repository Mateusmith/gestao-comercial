package br.com.commercecore.reporting;

import java.math.BigDecimal;
import java.util.UUID;

public record ReplenishmentItemResponse(
        UUID skuId,
        String codigoSku,
        String nomeProduto,
        BigDecimal estoqueMinimo,
        BigDecimal saldoFisico,
        BigDecimal saldoReservado,
        BigDecimal saldoDisponivel,
        BigDecimal quantidadeSugerida
) {
}
