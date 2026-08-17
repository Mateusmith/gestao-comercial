package br.com.commercecore.finance;

import br.com.commercecore.shared.Dinheiro;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialTitleResponse(
        UUID id,
        String numero,
        UUID empresaId,
        UUID filialId,
        UUID parceiroId,
        String parceiroNome,
        TitleType tipo,
        String tipoOrigem,
        UUID origemId,
        String documentoOrigem,
        int parcela,
        int totalParcelas,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        Dinheiro valorOriginal,
        Dinheiro saldo,
        FinancialTitleStatus status,
        long versao
) {
}
