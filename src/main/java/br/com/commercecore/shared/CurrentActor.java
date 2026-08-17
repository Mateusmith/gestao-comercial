package br.com.commercecore.shared;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CurrentActor {

    public String id() {
        Jwt token = token();
        return token == null ? "sistema" : token.getSubject();
    }

    public String email() {
        Jwt token = token();
        return token == null ? "sistema@commercecore.local" : token.getClaimAsString("email");
    }

    public boolean temPapel(String papel) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null) {
            return false;
        }
        String autoridadeEsperada = papel.startsWith("ROLE_") ? papel : "ROLE_" + papel;
        return autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(autoridadeEsperada::equals);
    }

    public List<String> filiaisPermitidas() {
        Jwt token = token();
        if (token == null) {
            return List.of();
        }
        Object valor = token.getClaims().get("filiais");
        if (valor instanceof Collection<?> colecao) {
            return colecao.stream().map(String::valueOf).toList();
        }
        return valor == null ? List.of() : List.of(String.valueOf(valor));
    }

    private Jwt token() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return autenticacao != null && autenticacao.getPrincipal() instanceof Jwt jwt ? jwt : null;
    }
}
