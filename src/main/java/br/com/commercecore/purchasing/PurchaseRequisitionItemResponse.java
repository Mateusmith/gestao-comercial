package br.com.commercecore.purchasing;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseRequisitionItemResponse(
        UUID skuId,
        String codigoSku,
        String nomeProduto,
        BigDecimal quantidade
) {
}
