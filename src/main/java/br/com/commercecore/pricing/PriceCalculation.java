package br.com.commercecore.pricing;

import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.UUID;

public record PriceCalculation(
        UUID tabelaPrecoId,
        String tabelaPreco,
        UUID skuId,
        BigDecimal quantidade,
        Dinheiro precoUnitarioBase,
        Dinheiro descontoUnitario,
        Dinheiro precoUnitarioFinal,
        Dinheiro subtotal,
        Dinheiro custoUnitarioReferencia,
        BigDecimal margemPercentual,
        UUID promocaoId,
        String promocao,
        String codigoCupom
) {
}
