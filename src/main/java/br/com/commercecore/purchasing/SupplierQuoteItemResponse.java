package br.com.commercecore.purchasing;

import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.util.UUID;

public record SupplierQuoteItemResponse(
        UUID skuId,
        BigDecimal quantidade,
        Dinheiro custoUnitario,
        Dinheiro subtotal
) {
}
