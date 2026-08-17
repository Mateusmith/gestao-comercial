package br.com.commercecore.inventory.internal;

import br.com.commercecore.inventory.MovementType;
import br.com.commercecore.inventory.StockOriginType;
import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimentacao_estoque")
public class StockMovementEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "deposito_id", nullable = false)
    private UUID depositoId;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "lote", nullable = false, length = 60)
    private String lote;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private MovementType tipo;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "saldo_fisico_anterior", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoFisicoAnterior;

    @Column(name = "saldo_fisico_posterior", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoFisicoPosterior;

    @Column(name = "saldo_reservado_anterior", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoReservadoAnterior;

    @Column(name = "saldo_reservado_posterior", nullable = false, precision = 19, scale = 3)
    private BigDecimal saldoReservadoPosterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 30)
    private StockOriginType tipoOrigem;

    @Column(name = "origem_id", nullable = false)
    private UUID origemId;

    @Column(name = "justificativa", nullable = false, length = 300)
    private String justificativa;

    @Column(name = "realizado_por", nullable = false, length = 120)
    private String realizadoPor;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected StockMovementEntity() {
    }

    public StockMovementEntity(
            UUID empresaId,
            UUID filialId,
            UUID depositoId,
            UUID skuId,
            String lote,
            MovementType tipo,
            BigDecimal quantidade,
            BigDecimal saldoFisicoAnterior,
            BigDecimal saldoFisicoPosterior,
            BigDecimal saldoReservadoAnterior,
            BigDecimal saldoReservadoPosterior,
            StockOriginType tipoOrigem,
            UUID origemId,
            String justificativa,
            String realizadoPor,
            Instant ocorridoEm) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.depositoId = depositoId;
        this.skuId = skuId;
        this.lote = lote;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.saldoFisicoAnterior = saldoFisicoAnterior;
        this.saldoFisicoPosterior = saldoFisicoPosterior;
        this.saldoReservadoAnterior = saldoReservadoAnterior;
        this.saldoReservadoPosterior = saldoReservadoPosterior;
        this.tipoOrigem = tipoOrigem;
        this.origemId = origemId;
        this.justificativa = justificativa;
        this.realizadoPor = realizadoPor;
        this.ocorridoEm = ocorridoEm;
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

    public MovementType getTipo() {
        return tipo;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public BigDecimal getSaldoFisicoAnterior() {
        return saldoFisicoAnterior;
    }

    public BigDecimal getSaldoFisicoPosterior() {
        return saldoFisicoPosterior;
    }

    public BigDecimal getSaldoReservadoAnterior() {
        return saldoReservadoAnterior;
    }

    public BigDecimal getSaldoReservadoPosterior() {
        return saldoReservadoPosterior;
    }

    public StockOriginType getTipoOrigem() {
        return tipoOrigem;
    }

    public UUID getOrigemId() {
        return origemId;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getRealizadoPor() {
        return realizadoPor;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
