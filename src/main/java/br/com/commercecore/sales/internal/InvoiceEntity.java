package br.com.commercecore.sales.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fatura_venda")
public class InvoiceEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID pedidoId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total;

    @Column(name = "emitida_em", nullable = false)
    private Instant emitidaEm;

    protected InvoiceEntity() {
    }

    public InvoiceEntity(
            String numero, UUID pedidoId, UUID empresaId, UUID filialId, Dinheiro total, Instant emitidaEm) {
        this.numero = numero;
        this.pedidoId = pedidoId;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.total = total;
        this.emitidaEm = emitidaEm;
    }

    public String getNumero() {
        return numero;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public Dinheiro getTotal() {
        return total;
    }

    public Instant getEmitidaEm() {
        return emitidaEm;
    }
}
