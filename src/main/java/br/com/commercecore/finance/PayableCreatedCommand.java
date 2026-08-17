package br.com.commercecore.finance;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PayableCreatedCommand(
        UUID eventoId,
        UUID origemId,
        String documentoOrigem,
        UUID empresaId,
        UUID filialId,
        UUID fornecedorId,
        String fornecedorNome,
        BigDecimal valorTotal,
        int numeroParcelas,
        LocalDate primeiroVencimento,
        Instant ocorridoEm
) {
}
