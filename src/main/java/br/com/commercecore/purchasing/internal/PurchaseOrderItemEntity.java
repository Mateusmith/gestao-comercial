package br.com.commercecore.purchasing.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
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
@Table(name = "item_pedido_compra")
public class PurchaseOrderItemEntity extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PurchaseOrderEntity pedido;
    @Column(name = "sku_id", nullable = false)
    private UUID skuId;
    @Column(name = "codigo_sku", nullable = false, length = 50)
    private String codigoSku;
    @Column(name = "nome_produto", nullable = false, length = 160)
    private String nomeProduto;
    @Column(name = "quantidade_pedida", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidadePedida;
    @Column(name = "quantidade_recebida", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidadeRecebida = BigDecimal.ZERO.setScale(3);
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "custo_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro custoUnitario;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", precision = 19, scale = 2, nullable = false))
    private Dinheiro subtotal;

    protected PurchaseOrderItemEntity() {
    }

    public PurchaseOrderItemEntity(
            UUID skuId, String codigoSku, String nomeProduto, BigDecimal quantidadePedida, Dinheiro custoUnitario) {
        this.skuId = skuId;
        this.codigoSku = codigoSku;
        this.nomeProduto = nomeProduto;
        this.quantidadePedida = quantidadePedida;
        this.custoUnitario = custoUnitario;
        this.subtotal = custoUnitario.multiplicar(quantidadePedida);
    }

    void vincular(PurchaseOrderEntity pedido) { this.pedido = pedido; }

    public void receber(BigDecimal quantidade) {
        BigDecimal novoTotal = quantidadeRecebida.add(quantidade);
        if (quantidade.signum() <= 0 || novoTotal.compareTo(quantidadePedida) > 0) {
            throw new BusinessRuleException(
                    "QUANTIDADE_RECEBIDA_INVALIDA", "A quantidade recebida deve ser positiva e nao exceder o pedido.");
        }
        quantidadeRecebida = novoTotal;
    }

    public boolean completamenteRecebido() { return quantidadeRecebida.compareTo(quantidadePedida) == 0; }
    public BigDecimal quantidadePendente() { return quantidadePedida.subtract(quantidadeRecebida); }
    public UUID getSkuId() { return skuId; }
    public String getCodigoSku() { return codigoSku; }
    public String getNomeProduto() { return nomeProduto; }
    public BigDecimal getQuantidadePedida() { return quantidadePedida; }
    public BigDecimal getQuantidadeRecebida() { return quantidadeRecebida; }
    public Dinheiro getCustoUnitario() { return custoUnitario; }
    public Dinheiro getSubtotal() { return subtotal; }
}
