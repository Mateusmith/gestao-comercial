package br.com.commercecore.purchasing.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "item_requisicao_compra")
public class PurchaseRequisitionItemEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requisicao_id", nullable = false)
    private PurchaseRequisitionEntity requisicao;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "codigo_sku", nullable = false, length = 50)
    private String codigoSku;

    @Column(name = "nome_produto", nullable = false, length = 160)
    private String nomeProduto;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;

    protected PurchaseRequisitionItemEntity() {
    }

    public PurchaseRequisitionItemEntity(UUID skuId, String codigoSku, String nomeProduto, BigDecimal quantidade) {
        this.skuId = skuId;
        this.codigoSku = codigoSku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
    }

    void vincular(PurchaseRequisitionEntity requisicao) { this.requisicao = requisicao; }
    public UUID getSkuId() { return skuId; }
    public String getCodigoSku() { return codigoSku; }
    public String getNomeProduto() { return nomeProduto; }
    public BigDecimal getQuantidade() { return quantidade; }
}
