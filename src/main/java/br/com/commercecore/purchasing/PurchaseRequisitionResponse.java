package br.com.commercecore.purchasing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PurchaseRequisitionResponse(
        UUID id,
        String numero,
        UUID empresaId,
        UUID filialId,
        String justificativa,
        String solicitadaPor,
        String aprovadaPor,
        Instant aprovadaEm,
        RequisitionStatus status,
        List<PurchaseRequisitionItemResponse> itens,
        long versao
) {
}
