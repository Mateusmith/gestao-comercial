package br.com.commercecore.catalog.internal;

import br.com.commercecore.catalog.MeasurementUnit;
import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Entity
@Table(name = "sku", uniqueConstraints = {
        @UniqueConstraint(name = "uq_sku_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uq_sku_codigo_barras", columnNames = "codigo_barras")
})
public class SkuEntity extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProductEntity produto;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Column(name = "descricao_variacao", length = 120)
    private String descricaoVariacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade_medida", nullable = false, length = 20)
    private MeasurementUnit unidadeMedida;

    @Column(name = "controla_lote", nullable = false)
    private boolean controlaLote;

    @Column(name = "aceita_fracionado", nullable = false)
    private boolean aceitaFracionado;

    @Column(name = "estoque_minimo", nullable = false, precision = 19, scale = 3)
    private BigDecimal estoqueMinimo;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    protected SkuEntity() {
    }

    public SkuEntity(
            String codigo,
            String codigoBarras,
            String descricaoVariacao,
            MeasurementUnit unidadeMedida,
            boolean controlaLote,
            boolean aceitaFracionado,
            BigDecimal estoqueMinimo) {
        this.codigo = codigo;
        this.codigoBarras = codigoBarras;
        this.descricaoVariacao = descricaoVariacao;
        this.unidadeMedida = unidadeMedida;
        this.controlaLote = controlaLote;
        this.aceitaFracionado = aceitaFracionado;
        this.estoqueMinimo = estoqueMinimo;
    }

    void vincular(ProductEntity produto) {
        this.produto = produto;
    }

    public ProductEntity getProduto() {
        return produto;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public String getDescricaoVariacao() {
        return descricaoVariacao;
    }

    public MeasurementUnit getUnidadeMedida() {
        return unidadeMedida;
    }

    public boolean isControlaLote() {
        return controlaLote;
    }

    public boolean isAceitaFracionado() {
        return aceitaFracionado;
    }

    public BigDecimal getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
