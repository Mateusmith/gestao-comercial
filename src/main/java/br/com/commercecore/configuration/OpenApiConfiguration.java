package br.com.commercecore.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI documentacaoApi() {
        var fluxo = new OAuthFlow()
                .authorizationUrl("http://localhost:18083/realms/commercecore/protocol/openid-connect/auth")
                .tokenUrl("http://localhost:18083/realms/commercecore/protocol/openid-connect/token");

        return new OpenAPI()
                .info(new Info()
                        .title("Gestão Comercial API")
                        .version("1.0.0")
                        .description("ERP comercial modular: vendas, compras, estoque, precificacao e financeiro.")
                        .contact(new Contact().name("Mateus Smith")))
                .components(new Components().addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().authorizationCode(fluxo))));
    }
}
