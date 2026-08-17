package br.com.commercecore.sales;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuoteResponse(
        UUID id,
        String numero,
        UUID empresaId,
        UUID filialId,
        UUID clienteId,
        String clienteNome,
        QuoteStatus status,
        Instant validoAte,
        String codigoCupom,
        Dinheiro total,
        List<QuoteItemResponse> itens,
        long versao
) {
}
