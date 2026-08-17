package br.com.commercecore.pricing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PromotionResponse(
        UUID id,
        UUID empresaId,
        UUID filialId,
        UUID skuId,
        String nome,
        String codigoCupom,
        DiscountType tipoDesconto,
        BigDecimal valorDesconto,
        BigDecimal quantidadeMinima,
        Instant inicio,
        Instant fim,
        int prioridade,
        boolean ativa
) {
}
