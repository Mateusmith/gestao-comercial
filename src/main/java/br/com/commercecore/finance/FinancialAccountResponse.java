package br.com.commercecore.finance;

import br.com.commercecore.shared.Dinheiro;
import java.util.UUID;

public record FinancialAccountResponse(
        UUID id,
        UUID empresaId,
        UUID filialId,
        String codigo,
        String nome,
        Dinheiro saldo,
        boolean ativa,
        long versao
) {
}
