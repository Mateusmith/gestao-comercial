package br.com.commercecore.shared;

public class ConflictException extends RuntimeException {

    private final String codigo;

    public ConflictException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
