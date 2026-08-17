package br.com.commercecore.shared;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Embeddable
public final class Quantidade implements Comparable<Quantidade> {

    public static final int ESCALA = 3;

    @Column(name = "quantidade", precision = 19, scale = ESCALA, nullable = false)
    private BigDecimal valor = BigDecimal.ZERO.setScale(ESCALA);

    protected Quantidade() {
    }

    private Quantidade(BigDecimal valor) {
        this.valor = Objects.requireNonNull(valor).setScale(ESCALA, RoundingMode.UNNECESSARY);
    }

    public static Quantidade de(BigDecimal valor) {
        return new Quantidade(valor);
    }

    public static Quantidade zero() {
        return de(BigDecimal.ZERO);
    }

    public Quantidade somar(Quantidade outra) {
        return de(valor.add(outra.valor));
    }

    public Quantidade subtrair(Quantidade outra) {
        return de(valor.subtract(outra.valor));
    }

    public boolean positiva() {
        return valor.signum() > 0;
    }

    public boolean negativa() {
        return valor.signum() < 0;
    }

    @JsonValue
    public BigDecimal valor() {
        return valor;
    }

    @Override
    public int compareTo(Quantidade outra) {
        return valor.compareTo(outra.valor);
    }

    @Override
    public boolean equals(Object objeto) {
        return objeto instanceof Quantidade quantidade && valor.compareTo(quantidade.valor) == 0;
    }

    @Override
    public int hashCode() {
        return valor.stripTrailingZeros().hashCode();
    }
}
