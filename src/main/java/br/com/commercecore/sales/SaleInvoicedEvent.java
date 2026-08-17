package br.com.commercecore.sales;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SaleInvoicedEvent(
        UUID eventoId,
        UUID faturaId,
        String numeroFatura,
        UUID pedidoId,
        UUID empresaId,
        UUID filialId,
        UUID clienteId,
        String clienteNome,
        BigDecimal valorTotal,
        int numeroParcelas,
        LocalDate primeiroVencimento,
        Instant faturadoEm
) {
}
