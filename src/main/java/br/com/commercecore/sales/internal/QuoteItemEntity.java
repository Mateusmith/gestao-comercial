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
@Table(name = "item_orcamento_venda")
public class QuoteItemEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false)
    private QuoteEntity orcamento;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "codigo_sku", nullable = false, length = 50)
    private String codigoSku;

    @Column(name = "nome_produto", nullable = false, length = 160)
    private String nomeProduto;

    @Column(name = "quantidade", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidade;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco_unitario_base", precision = 19, scale = 2, nullable = false))
    private Dinheiro precoUnitarioBase;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "desconto_unitario", precision = 19, scale = 2, nullable = false))
    private Dinheiro descontoUnitario;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "preco_unitario_final", precision = 19, scale = 2, nullable = false))
    private Dinheiro precoUnitarioFinal;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "subtotal", precision = 19, scale = 2, nullable = false))
    private Dinheiro subtotal;

    @Column(name = "tabela_preco_id", nullable = false)
    private UUID tabelaPrecoId;

    @Column(name = "promocao_id")
    private UUID promocaoId;

    protected QuoteItemEntity() {
    }

    public QuoteItemEntity(
            UUID skuId,
            String codigoSku,
            String nomeProduto,
            BigDecimal quantidade,
            Dinheiro precoUnitarioBase,
            Dinheiro descontoUnitario,
            Dinheiro precoUnitarioFinal,
            Dinheiro subtotal,
            UUID tabelaPrecoId,
            UUID promocaoId) {
        this.skuId = skuId;
        this.codigoSku = codigoSku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitarioBase = precoUnitarioBase;
        this.descontoUnitario = descontoUnitario;
        this.precoUnitarioFinal = precoUnitarioFinal;
        this.subtotal = subtotal;
        this.tabelaPrecoId = tabelaPrecoId;
        this.promocaoId = promocaoId;
    }

    void vincular(QuoteEntity orcamento) {
        this.orcamento = orcamento;
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

    public Dinheiro getPrecoUnitarioBase() {
        return precoUnitarioBase;
    }

    public Dinheiro getDescontoUnitario() {
        return descontoUnitario;
    }

    public Dinheiro getPrecoUnitarioFinal() {
        return precoUnitarioFinal;
    }

    public Dinheiro getSubtotal() {
        return subtotal;
    }

    public UUID getTabelaPrecoId() {
        return tabelaPrecoId;
    }

    public UUID getPromocaoId() {
        return promocaoId;
    }
}
