package br.com.commercecore.purchasing.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "item_recebimento_compra")
public class PurchaseReceiptItemEntity extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recebimento_id", nullable = false)
    private PurchaseReceiptEntity recebimento;
    @Column(name = "sku_id", nullable = false)
    private UUID skuId;
    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;
    @Column(name = "lote", length = 60)
    private String lote;
    @Column(name = "validade_lote")
    private LocalDate validadeLote;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "custo_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro custoUnitario;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", precision = 19, scale = 2, nullable = false))
    private Dinheiro subtotal;

    protected PurchaseReceiptItemEntity() {
    }

    public PurchaseReceiptItemEntity(
            UUID skuId, BigDecimal quantidade, String lote, LocalDate validadeLote, Dinheiro custoUnitario) {
        this.skuId = skuId;
        this.quantidade = quantidade;
        this.lote = lote;
        this.validadeLote = validadeLote;
        this.custoUnitario = custoUnitario;
        this.subtotal = custoUnitario.multiplicar(quantidade);
    }

    void vincular(PurchaseReceiptEntity recebimento) { this.recebimento = recebimento; }
    public UUID getSkuId() { return skuId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public String getLote() { return lote; }
    public LocalDate getValidadeLote() { return validadeLote; }
    public Dinheiro getCustoUnitario() { return custoUnitario; }
    public Dinheiro getSubtotal() { return subtotal; }
}
