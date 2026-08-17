package br.com.commercecore.purchasing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PurchaseReceivedEvent(
        UUID eventoId,
        UUID recebimentoId,
        String numeroRecebimento,
        UUID pedidoId,
        UUID empresaId,
        UUID filialId,
        UUID fornecedorId,
        String fornecedorNome,
        BigDecimal valorTotal,
        int numeroParcelas,
        LocalDate primeiroVencimento,
        Instant recebidoEm
) {
}
