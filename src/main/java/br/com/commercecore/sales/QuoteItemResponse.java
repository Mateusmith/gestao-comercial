package br.com.commercecore.sales;

import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemResponse(
        UUID skuId,
        String codigoSku,
        String nomeProduto,
        BigDecimal quantidade,
        Dinheiro precoUnitarioBase,
        Dinheiro descontoUnitario,
        Dinheiro precoUnitarioFinal,
        Dinheiro subtotal,
        UUID tabelaPrecoId,
        UUID promocaoId
) {
}
