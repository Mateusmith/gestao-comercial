package br.com.commercecore.reporting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AgingResponse(
        UUID empresaId,
        UUID filialId,
        LocalDate dataReferencia,
        BigDecimal aVencer,
        BigDecimal vencidoAte30Dias,
        BigDecimal vencido31A60Dias,
        BigDecimal vencido61A90Dias,
        BigDecimal vencidoAcima90Dias,
        BigDecimal totalEmAberto
) {
}
