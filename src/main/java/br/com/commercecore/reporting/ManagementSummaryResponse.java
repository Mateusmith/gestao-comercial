package br.com.commercecore.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ManagementSummaryResponse(
        UUID empresaId,
        UUID filialId,
        LocalDate inicio,
        LocalDate fim,
        long vendasFaturadas,
        BigDecimal faturamento,
        BigDecimal comprasRecebidas,
        BigDecimal contasReceberEmAberto,
        BigDecimal contasPagarEmAberto,
        long skusParaReposicao
) {
}
