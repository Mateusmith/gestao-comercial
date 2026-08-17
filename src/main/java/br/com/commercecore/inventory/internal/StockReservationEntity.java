package br.com.commercecore.inventory.internal;

import br.com.commercecore.inventory.ReservationStatus;
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
@Table(name = "reserva_estoque")
public class StockReservationEntity extends AbstractEntity {

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

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origem", nullable = false, length = 30)
    private StockOriginType tipoOrigem;

    @Column(name = "origem_id", nullable = false)
    private UUID origemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.ATIVA;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    protected StockReservationEntity() {
    }

    public StockReservationEntity(
            UUID empresaId,
            UUID filialId,
            UUID depositoId,
            UUID skuId,
            String lote,
            BigDecimal quantidade,
            StockOriginType tipoOrigem,
            UUID origemId,
            Instant expiraEm) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.depositoId = depositoId;
        this.skuId = skuId;
        this.lote = lote;
        this.quantidade = quantidade;
        this.tipoOrigem = tipoOrigem;
        this.origemId = origemId;
        this.expiraEm = expiraEm;
    }

    public void consumir() {
        status = ReservationStatus.CONSUMIDA;
    }

    public void liberar(boolean expirada) {
        status = expirada ? ReservationStatus.EXPIRADA : ReservationStatus.LIBERADA;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
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

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public StockOriginType getTipoOrigem() {
        return tipoOrigem;
    }

    public UUID getOrigemId() {
        return origemId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getExpiraEm() {
        return expiraEm;
    }
}
