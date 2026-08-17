package br.com.commercecore.inventory.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "saldo_estoque")
public class StockBalanceEntity extends AbstractEntity {

    @Column(name = "deposito_id", nullable = false)
    private UUID depositoId;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "lote", nullable = false, length = 60)
    private String lote;

    @Column(name = "validade_lote")
    private LocalDate validadeLote;

    @Column(name = "saldo_fisico", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoFisico = zero();

    @Column(name = "saldo_reservado", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoReservado = zero();

    protected StockBalanceEntity() {
    }

    public StockBalanceEntity(UUID depositoId, UUID skuId, String lote, LocalDate validadeLote) {
        this.depositoId = depositoId;
        this.skuId = skuId;
        this.lote = lote;
        this.validadeLote = validadeLote;
    }

    public void entrar(BigDecimal quantidade) {
        saldoFisico = saldoFisico.add(normalizar(quantidade));
    }

    public void sair(BigDecimal quantidade) {
        BigDecimal valor = normalizar(quantidade);
        if (disponivel().compareTo(valor) < 0) {
            throw new BusinessRuleException("SALDO_INSUFICIENTE", "O saldo disponivel e insuficiente para a saida.");
        }
        saldoFisico = saldoFisico.subtract(valor);
    }

    public void reservar(BigDecimal quantidade) {
        BigDecimal valor = normalizar(quantidade);
        if (disponivel().compareTo(valor) < 0) {
            throw new BusinessRuleException("SALDO_INSUFICIENTE", "O saldo disponivel e insuficiente para a reserva.");
        }
        saldoReservado = saldoReservado.add(valor);
    }

    public void consumirReserva(BigDecimal quantidade) {
        BigDecimal valor = normalizar(quantidade);
        if (saldoReservado.compareTo(valor) < 0 || saldoFisico.compareTo(valor) < 0) {
            throw new BusinessRuleException("RESERVA_INCONSISTENTE", "A reserva nao pode ser consumida no saldo atual.");
        }
        saldoReservado = saldoReservado.subtract(valor);
        saldoFisico = saldoFisico.subtract(valor);
    }

    public void liberarReserva(BigDecimal quantidade) {
        BigDecimal valor = normalizar(quantidade);
        if (saldoReservado.compareTo(valor) < 0) {
            throw new BusinessRuleException("RESERVA_INCONSISTENTE", "A quantidade liberada supera o saldo reservado.");
        }
        saldoReservado = saldoReservado.subtract(valor);
    }

    public BigDecimal disponivel() {
        return saldoFisico.subtract(saldoReservado);
    }

    public UUID getDepositoId() {
        return depositoId;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public String getLote() {
        return lote;
    }

    public LocalDate getValidadeLote() {
        return validadeLote;
    }

    public BigDecimal getSaldoFisico() {
        return saldoFisico;
    }

    public BigDecimal getSaldoReservado() {
        return saldoReservado;
    }

    private static BigDecimal normalizar(BigDecimal valor) {
        return valor.setScale(3, RoundingMode.UNNECESSARY);
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(3);
    }
}
