package br.com.commercecore.pricing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PriceTableResponse(
        UUID id,
        UUID empresaId,
        UUID filialId,
        String nome,
        Instant vigenteDe,
        Instant vigenteAte,
        boolean ativa,
        List<PriceTableItemResponse> itens
) {
}
