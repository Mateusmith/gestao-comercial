package br.com.commercecore.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail naoEncontrado(NotFoundException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.NOT_FOUND, "Recurso nao encontrado", excecao.getMessage(), "RECURSO_NAO_ENCONTRADO", requisicao);
    }

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail regraNegocio(BusinessRuleException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Regra de negocio violada", excecao.getMessage(), excecao.codigo(), requisicao);
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflito(ConflictException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.CONFLICT, "Conflito de operacao", excecao.getMessage(), excecao.codigo(), requisicao);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concorrencia(ObjectOptimisticLockingFailureException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.CONFLICT, "Registro alterado", "O recurso foi alterado por outro usuario. Recarregue os dados e tente novamente.", "VERSAO_DESATUALIZADA", requisicao);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados", "A operacao viola uma restricao de integridade ou unicidade.", "DADOS_CONFLITANTES", requisicao);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException excecao, HttpServletRequest requisicao) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados invalidos", "Um ou mais campos precisam ser corrigidos.", "VALIDACAO_FALHOU", requisicao);
        Map<String, String> campos = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors().forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        problema.setProperty("campos", campos);
        return problema;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail restricao(ConstraintViolationException excecao, HttpServletRequest requisicao) {
        return problema(HttpStatus.BAD_REQUEST, "Dados invalidos", excecao.getMessage(), "VALIDACAO_FALHOU", requisicao);
    }

    private ProblemDetail problema(
            HttpStatus status,
            String titulo,
            String detalhe,
            String codigo,
            HttpServletRequest requisicao) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create("https://commercecore.local/problemas/" + codigo.toLowerCase()));
        problema.setInstance(URI.create(requisicao.getRequestURI()));
        problema.setProperty("codigo", codigo);
        problema.setProperty("correlacao", MDC.get("correlationId"));
        return problema;
    }
}
