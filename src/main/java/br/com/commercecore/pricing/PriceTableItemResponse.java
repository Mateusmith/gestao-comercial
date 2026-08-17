package br.com.commercecore.pricing;

import br.com.commercecore.shared.Dinheiro;
import java.util.UUID;

public record PriceTableItemResponse(
        UUID skuId,
        Dinheiro valorVenda,
        Dinheiro custoReferencia
) {
}
