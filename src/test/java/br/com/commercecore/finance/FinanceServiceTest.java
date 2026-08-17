package br.com.commercecore.finance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.commercecore.shared.BusinessRuleException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class FinanceServiceTest {

    @Test
    void deveDistribuirCentavosSemAlterarOValorTotal() {
        var parcelas = FinanceService.dividir(new BigDecimal("100.00"), 3);

        assertThat(parcelas).extracting(valor -> valor.valor().toPlainString())
                .containsExactly("33.34", "33.33", "33.33");
        assertThat(parcelas.stream()
                .map(valor -> valor.valor())
                .reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void deveImpedirParcelasComValorZero() {
        assertThatThrownBy(() -> FinanceService.dividir(new BigDecimal("0.01"), 2))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(excecao -> assertThat(((BusinessRuleException) excecao).codigo())
                        .isEqualTo("PARCELAMENTO_SUPERIOR_AO_VALOR"));
    }
}
