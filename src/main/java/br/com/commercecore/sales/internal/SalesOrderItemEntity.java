package br.com.commercecore.sales.internal;

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
@Table(name = "item_pedido_venda")
public class SalesOrderItemEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private SalesOrderEntity pedido;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "codigo_sku", nullable = false, length = 50)
    private String codigoSku;

    @Column(name = "nome_produto", nullable = false, length = 160)
    private String nomeProduto;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro precoUnitario;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "desconto_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro descontoUnitario;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", precision = 19, scale = 2, nullable = false))
    private Dinheiro subtotal;

    protected SalesOrderItemEntity() {
    }

    public SalesOrderItemEntity(
            UUID skuId,
            String codigoSku,
            String nomeProduto,
            BigDecimal quantidade,
            Dinheiro precoUnitario,
            Dinheiro descontoUnitario,
            Dinheiro subtotal) {
        this.skuId = skuId;
        this.codigoSku = codigoSku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.descontoUnitario = descontoUnitario;
        this.subtotal = subtotal;
    }

    void vincular(SalesOrderEntity pedido) {
        this.pedido = pedido;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public String getCodigoSku() {
        return codigoSku;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public Dinheiro getPrecoUnitario() {
        return precoUnitario;
    }

    public Dinheiro getDescontoUnitario() {
        return descontoUnitario;
    }

    public Dinheiro getSubtotal() {
        return subtotal;
    }
}
