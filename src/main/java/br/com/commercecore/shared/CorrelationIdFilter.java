package br.com.commercecore.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        String correlacao = requisicao.getHeader(CABECALHO);
        if (correlacao == null || correlacao.isBlank() || correlacao.length() > 100) {
            correlacao = UUID.randomUUID().toString();
        }

        try {
            MDC.put("correlationId", correlacao);
            resposta.setHeader(CABECALHO, correlacao);
            cadeia.doFilter(requisicao, resposta);
        } finally {
            MDC.remove("correlationId");
        }
    }
}
