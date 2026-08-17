package br.com.commercecore.sales.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.commercecore.sales.SalesOrderStatus;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Dinheiro;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SalesOrderEntityTest {

    @Test
    void deveSomarItensEExecutarFluxoAteOFaturamento() {
        SalesOrderEntity pedido = novoPedido();
        pedido.adicionarItem(new SalesOrderItemEntity(
                UUID.randomUUID(), "SKU-1", "Produto", new BigDecimal("2.000"),
                Dinheiro.de("50.00"), Dinheiro.de("5.00"), Dinheiro.de("90.00")));

        Instant confirmacao = Instant.parse("2026-08-16T10:00:00Z");
        Instant faturamento = Instant.parse("2026-08-16T11:00:00Z");
        pedido.confirmar(confirmacao);
        pedido.faturar(faturamento);

        assertThat(pedido.getTotal().valor()).isEqualByComparingTo("90.00");
        assertThat(pedido.getStatus()).isEqualTo(SalesOrderStatus.FATURADO);
        assertThat(pedido.getConfirmadoEm()).isEqualTo(confirmacao);
        assertThat(pedido.getFaturadoEm()).isEqualTo(faturamento);
        assertThatThrownBy(() -> pedido.cancelar(faturamento.plusSeconds(1)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void naoDeveConfirmarPedidoCancelado() {
        SalesOrderEntity pedido = novoPedido();
        pedido.cancelar(Instant.parse("2026-08-16T10:00:00Z"));

        assertThatThrownBy(() -> pedido.confirmar(Instant.parse("2026-08-16T11:00:00Z")))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(excecao -> assertThat(((BusinessRuleException) excecao).codigo())
                        .isEqualTo("STATUS_PEDIDO_INVALIDO"));
    }

    private static SalesOrderEntity novoPedido() {
        return new SalesOrderEntity(
                "PV-0001", UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Cliente", 2, LocalDate.of(2026, 9, 1));
    }
}
