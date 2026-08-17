package br.com.commercecore.inventory.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.commercecore.shared.BusinessRuleException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockBalanceEntityTest {

    @Test
    void deveSepararSaldoFisicoReservadoEDisponivel() {
        StockBalanceEntity saldo = novoSaldo();
        saldo.entrar(new BigDecimal("10.000"));
        saldo.reservar(new BigDecimal("3.000"));

        assertThat(saldo.getSaldoFisico()).isEqualByComparingTo("10.000");
        assertThat(saldo.getSaldoReservado()).isEqualByComparingTo("3.000");
        assertThat(saldo.disponivel()).isEqualByComparingTo("7.000");

        saldo.consumirReserva(new BigDecimal("2.000"));

        assertThat(saldo.getSaldoFisico()).isEqualByComparingTo("8.000");
        assertThat(saldo.getSaldoReservado()).isEqualByComparingTo("1.000");
        assertThat(saldo.disponivel()).isEqualByComparingTo("7.000");
    }

    @Test
    void deveImpedirReservaAcimaDoDisponivel() {
        StockBalanceEntity saldo = novoSaldo();
        saldo.entrar(new BigDecimal("2.000"));

        assertThatThrownBy(() -> saldo.reservar(new BigDecimal("2.001")))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(excecao -> assertThat(((BusinessRuleException) excecao).codigo())
                        .isEqualTo("SALDO_INSUFICIENTE"));
    }

    private static StockBalanceEntity novoSaldo() {
        return new StockBalanceEntity(UUID.randomUUID(), UUID.randomUUID(), "SEM_LOTE", null);
    }
}
