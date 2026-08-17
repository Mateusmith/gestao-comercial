package br.com.commercecore.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DinheiroTest {

    @Test
    void deveNormalizarOperacoesMonetariasEmDuasCasas() {
        Dinheiro preco = Dinheiro.de("19.995");
        Dinheiro total = preco.multiplicar(new BigDecimal("3"));

        assertThat(preco.valor()).isEqualByComparingTo("20.00");
        assertThat(total.valor()).isEqualByComparingTo("60.00");
    }

    @Test
    void deveCalcularPercentualComArredondamentoFinanceiro() {
        Dinheiro desconto = Dinheiro.de("99.90").percentual(new BigDecimal("12.5"));

        assertThat(desconto.valor()).isEqualByComparingTo("12.49");
    }

    @Test
    void igualdadeDeveIgnorarDiferencaDeEscala() {
        assertThat(Dinheiro.de("10.0"))
                .isEqualTo(Dinheiro.de("10.00"))
                .hasSameHashCodeAs(Dinheiro.de("10.000"));
    }
}
