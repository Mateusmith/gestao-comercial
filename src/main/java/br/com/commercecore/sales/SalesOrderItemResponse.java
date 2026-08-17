package br.com.commercecore.sales;

import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
        UUID skuId,
        String codigoSku,
        String nomeProduto,
        BigDecimal quantidade,
        Dinheiro precoUnitario,
        Dinheiro descontoUnitario,
        Dinheiro subtotal
) {
}
