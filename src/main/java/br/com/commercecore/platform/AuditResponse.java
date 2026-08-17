package br.com.commercecore.platform;

import java.time.Instant;
import java.util.UUID;

public record AuditResponse(
        UUID id,
        UUID empresaId,
        String atorId,
        String metodoHttp,
        String caminho,
        String parametros,
        int statusHttp,
        String enderecoIp,
        String correlacao,
        Instant ocorridoEm
) {
}
