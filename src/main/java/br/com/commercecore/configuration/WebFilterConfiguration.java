package br.com.commercecore.configuration;

import br.com.commercecore.platform.AuditFilter;
import br.com.commercecore.shared.CorrelationIdFilter;
import br.com.commercecore.shared.CurrentActor;
import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
public class WebFilterConfiguration {

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        var registro = new FilterRegistrationBean<>(new CorrelationIdFilter());
        registro.setName("correlationIdFilter");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }

    @Bean
    FilterRegistrationBean<AuditFilter> auditFilter(
            ApplicationEventPublisher eventos, CurrentActor atorAtual, Clock relogio) {
        var registro = new FilterRegistrationBean<>(new AuditFilter(eventos, atorAtual, relogio));
        registro.setName("auditFilter");
        registro.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return registro;
    }
}
