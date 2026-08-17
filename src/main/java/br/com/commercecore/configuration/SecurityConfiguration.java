package br.com.commercecore.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    @Order(1)
    SecurityFilterChain filtroObservabilidade(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/prometheus")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacao -> autorizacao.anyRequest().hasRole("OBSERVABILIDADE"))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers("/", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(conversorJwt())))
                .headers(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    UserDetailsService usuariosObservabilidade(
            @Value("${commercecore.observabilidade.usuario}") String usuario,
            @Value("${commercecore.observabilidade.senha}") String senha) {
        return new InMemoryUserDetailsManager(User.withUsername(usuario)
                .password("{noop}" + senha)
                .roles("OBSERVABILIDADE")
                .build());
    }

    @Bean
    NimbusJwtDecoder decodificadorJwt(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String emissor,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String urlChaves,
            @Value("${commercecore.seguranca.audiencia}") String audiencia) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withJwkSetUri(urlChaves).build();
        OAuth2TokenValidator<Jwt> validador = new DelegatingOAuth2TokenValidator<>(
                new JwtIssuerValidator(emissor),
                new JwtAudienceValidator(audiencia));
        decodificador.setJwtValidator(validador);
        return decodificador;
    }

    private Converter<Jwt, AbstractAuthenticationToken> conversorJwt() {
        return jwt -> new JwtAuthenticationToken(jwt, autoridades(jwt), jwt.getClaimAsString("preferred_username"));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> autoridades(Jwt jwt) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        Map<String, Object> acessoRealm = jwt.getClaim("realm_access");
        if (acessoRealm != null && acessoRealm.get("roles") instanceof Collection<?> papeis) {
            papeis.stream()
                    .map(String::valueOf)
                    .map(String::toUpperCase)
                    .map(papel -> papel.startsWith("ROLE_") ? papel : "ROLE_" + papel)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(autoridades::add);
        }
        List<String> escopos = jwt.getClaimAsStringList("scope");
        if (escopos != null) {
            escopos.stream().map(escopo -> new SimpleGrantedAuthority("SCOPE_" + escopo)).forEach(autoridades::add);
        }
        return autoridades;
    }
}
