package br.com.commercecore.sales;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        String numero,
        UUID pedidoId,
        Dinheiro total,
        Instant emitidaEm
) {
}
