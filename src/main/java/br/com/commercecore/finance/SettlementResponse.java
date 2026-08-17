package br.com.commercecore.finance;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID tituloId,
        UUID contaFinanceiraId,
        SettlementKind tipo,
        UUID liquidacaoOriginalId,
        Dinheiro valor,
        PaymentMethod formaPagamento,
        String chaveIdempotencia,
        String observacao,
        String realizadoPor,
        Instant ocorridoEm,
        Dinheiro saldoTituloAposOperacao,
        Dinheiro saldoContaAposOperacao
) {
}
