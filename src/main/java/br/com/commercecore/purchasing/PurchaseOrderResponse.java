package br.com.commercecore.purchasing;

import br.com.commercecore.shared.Dinheiro;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderResponse(
        UUID id,
        String numero,
        UUID requisicaoId,
        UUID cotacaoId,
        UUID empresaId,
        UUID filialId,
        UUID depositoId,
        UUID fornecedorId,
        String fornecedorNome,
        PurchaseOrderStatus status,
        Dinheiro total,
        int numeroParcelas,
        LocalDate primeiroVencimento,
        Instant emitidoEm,
        Instant concluidoEm,
        List<PurchaseOrderItemResponse> itens,
        long versao
) {
}
