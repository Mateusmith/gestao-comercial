package br.com.commercecore.pricing;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePromotionRequest(
        @NotNull UUID empresaId,
        UUID filialId,
        @NotNull UUID skuId,
        @NotBlank @Size(max = 100) String nome,
        @Size(max = 40) String codigoCupom,
        @NotNull DiscountType tipoDesconto,
        @NotNull @DecimalMin(value = "0.01") BigDecimal valorDesconto,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantidadeMinima,
        @NotNull Instant inicio,
        @NotNull Instant fim,
        int prioridade
) {
}
