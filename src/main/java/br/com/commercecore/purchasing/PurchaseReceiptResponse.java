package br.com.commercecore.purchasing;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.util.UUID;

public record PurchaseReceiptResponse(
        UUID id,
        String numero,
        UUID pedidoId,
        String documentoFornecedor,
        Dinheiro valorTotal,
        Instant recebidoEm,
        String chaveIdempotencia
) {
}
