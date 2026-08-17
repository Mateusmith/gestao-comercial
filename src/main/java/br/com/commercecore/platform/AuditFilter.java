package br.com.commercecore.platform;

import br.com.commercecore.shared.CurrentActor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.filter.OncePerRequestFilter;

public class AuditFilter extends OncePerRequestFilter {

    private static final Set<String> METODOS_AUDITADOS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final ApplicationEventPublisher eventos;
    private final CurrentActor atorAtual;
    private final Clock relogio;

    public AuditFilter(ApplicationEventPublisher eventos, CurrentActor atorAtual, Clock relogio) {
        this.eventos = eventos;
        this.atorAtual = atorAtual;
        this.relogio = relogio;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !METODOS_AUDITADOS.contains(requisicao.getMethod())
                || !requisicao.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            eventos.publishEvent(new AuditRecordedEvent(
                    empresaId(requisicao.getHeader("X-Empresa-ID")),
                    atorAtual.id(), requisicao.getMethod(), limitar(requisicao.getRequestURI(), 300),
                    limitar(requisicao.getQueryString(), 1000), resposta.getStatus(),
                    limitar(enderecoIp(requisicao), 64),
                    limitar(MDC.get("correlationId"), 100),
                    relogio.instant()));
        }
    }

    private static UUID empresaId(String valor) {
        try {
            return valor == null ? null : UUID.fromString(valor);
        } catch (IllegalArgumentException excecao) {
            return null;
        }
    }

    private static String enderecoIp(HttpServletRequest requisicao) {
        String encaminhado = requisicao.getHeader("X-Forwarded-For");
        return encaminhado == null || encaminhado.isBlank()
                ? requisicao.getRemoteAddr()
                : encaminhado.split(",")[0].trim();
    }

    private static String limitar(String valor, int tamanho) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }
}
