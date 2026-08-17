package br.com.commercecore.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class ObservabilityConfigurationTest {

    @Test
    void deveManterOMesmoConjuntoDeDimensoesNasMetricasDeModulos() {
        var registro = new SimpleMeterRegistry();
        var configuracao = new ObservabilityConfiguration();
        configuracao.padronizarDimensoesDosModulos().customize(registro);

        var chamadaComum = registro.counter("module.requests", "module.key", "sales");
        var ouvinte = registro.counter(
                "module.requests",
                "module.key", "finance",
                "module.invocation-type", "event-listener");

        assertThat(chamadaComum.getId().getTag("module.invocation-type")).isEqualTo("bean");
        assertThat(ouvinte.getId().getTag("module.invocation-type")).isEqualTo("event-listener");
        registro.close();
    }
}
