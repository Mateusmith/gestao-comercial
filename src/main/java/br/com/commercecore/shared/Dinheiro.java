package br.com.commercecore.shared;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public final class Dinheiro implements Comparable<Dinheiro> {

    public static final int ESCALA = 2;

    @Column(name = "valor", precision = 19, scale = ESCALA, nullable = false)
    private BigDecimal valor = BigDecimal.ZERO.setScale(ESCALA);

    protected Dinheiro() {
    }

    private Dinheiro(BigDecimal valor) {
        this.valor = normalizar(valor);
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(Objects.requireNonNull(valor, "O valor monetario e obrigatorio."));
    }

    public static Dinheiro de(String valor) {
        return de(new BigDecimal(valor));
    }

    public static Dinheiro zero() {
        return de(BigDecimal.ZERO);
    }

    public Dinheiro somar(Dinheiro outro) {
        return de(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        return de(valor.subtract(outro.valor));
    }

    public Dinheiro multiplicar(BigDecimal fator) {
        return de(valor.multiply(fator));
    }

    public Dinheiro percentual(BigDecimal percentual) {
        return de(valor.multiply(percentual).divide(new BigDecimal("100"), ESCALA + 4, RoundingMode.HALF_UP));
    }

    public Dinheiro minimo(Dinheiro outro) {
        return compareTo(outro) <= 0 ? this : outro;
    }

    public Dinheiro maximoZero() {
        return negativo() ? zero() : this;
    }

    public boolean negativo() {
        return valor.signum() < 0;
    }

    public boolean positivo() {
        return valor.signum() > 0;
    }

    @JsonValue
    public BigDecimal valor() {
        return valor;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Dinheiro dinheiro && valor.compareTo(dinheiro.valor) == 0;
    }

    @Override
    public int hashCode() {
        return valor.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(ESCALA, RoundingMode.HALF_UP);
    }
}
