package br.com.commercecore.configuration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfiguration {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> padronizarDimensoesDosModulos() {
        return registro -> registro.config().meterFilter(new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id identificador) {
                boolean ehMetricaDeModulo = identificador.getName().startsWith("module.requests");
                boolean possuiTipoInvocacao = identificador.getTag("module.invocation-type") != null;

                return ehMetricaDeModulo && !possuiTipoInvocacao
                        ? identificador.withTag(Tag.of("module.invocation-type", "bean"))
                        : identificador;
            }
        });
    }
}
