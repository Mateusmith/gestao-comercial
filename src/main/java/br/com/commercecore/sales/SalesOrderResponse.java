package br.com.commercecore.sales;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SalesOrderResponse(
        UUID id,
        String numero,
        UUID orcamentoId,
        UUID empresaId,
        UUID filialId,
        UUID depositoId,
        UUID clienteId,
        String clienteNome,
        SalesOrderStatus status,
        Dinheiro total,
        int numeroParcelas,
        LocalDate primeiroVencimento,
        Instant confirmadoEm,
        Instant faturadoEm,
        Instant canceladoEm,
        List<SalesOrderItemResponse> itens,
        long versao
) {
}
