package br.com.commercecore.shared;

public class BusinessRuleException extends RuntimeException {

    private final String codigo;

    public BusinessRuleException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
