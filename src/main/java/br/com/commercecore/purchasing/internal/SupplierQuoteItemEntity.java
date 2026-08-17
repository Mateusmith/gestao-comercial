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
import java.util.UUID;

@Entity
@Table(name = "item_cotacao_fornecedor")
public class SupplierQuoteItemEntity extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cotacao_id", nullable = false)
    private SupplierQuoteEntity cotacao;
    @Column(name = "sku_id", nullable = false)
    private UUID skuId;
    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "custo_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro custoUnitario;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", precision = 19, scale = 2, nullable = false))
    private Dinheiro subtotal;

    protected SupplierQuoteItemEntity() {
    }

    public SupplierQuoteItemEntity(UUID skuId, BigDecimal quantidade, Dinheiro custoUnitario) {
        this.skuId = skuId;
        this.quantidade = quantidade;
        this.custoUnitario = custoUnitario;
        this.subtotal = custoUnitario.multiplicar(quantidade);
    }

    void vincular(SupplierQuoteEntity cotacao) { this.cotacao = cotacao; }
    public UUID getSkuId() { return skuId; }
    public BigDecimal getQuantidade() { return quantidade; }
    public Dinheiro getCustoUnitario() { return custoUnitario; }
    public Dinheiro getSubtotal() { return subtotal; }
}
