package br.com.commercecore.purchasing;

import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
        UUID skuId,
        String codigoSku,
        String nomeProduto,
        BigDecimal quantidadePedida,
        BigDecimal quantidadeRecebida,
        BigDecimal quantidadePendente,
        Dinheiro custoUnitario,
        Dinheiro subtotal
) {
}
