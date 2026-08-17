package br.com.commercecore.purchasing;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SupplierQuoteResponse(
        UUID id,
        String numero,
        UUID requisicaoId,
        UUID fornecedorId,
        String fornecedorNome,
        Instant validoAte,
        SupplierQuoteStatus status,
        Dinheiro total,
        List<SupplierQuoteItemResponse> itens,
        long versao
) {
}
