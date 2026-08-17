package br.com.commercecore.pricing.internal;

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
import java.util.UUID;

@Entity
@Table(name = "item_tabela_preco")
public class PriceTableItemEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tabela_preco_id", nullable = false)
    private PriceTableEntity tabelaPreco;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_venda", precision = 19, scale = 2, nullable = false))
    private Dinheiro valorVenda;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "custo_referencia", precision = 19, scale = 2, nullable = false))
    private Dinheiro custoReferencia;

    protected PriceTableItemEntity() {
    }

    public PriceTableItemEntity(UUID skuId, Dinheiro valorVenda, Dinheiro custoReferencia) {
        this.skuId = skuId;
        this.valorVenda = valorVenda;
        this.custoReferencia = custoReferencia;
    }

    void vincular(PriceTableEntity tabelaPreco) {
        this.tabelaPreco = tabelaPreco;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public Dinheiro getValorVenda() {
        return valorVenda;
    }

    public Dinheiro getCustoReferencia() {
        return custoReferencia;
    }
}
