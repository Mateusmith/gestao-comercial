package br.com.commercecore.pricing.internal;

import br.com.commercecore.pricing.DiscountType;
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
@Table(name = "promocao")
public class PromotionEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id")
    private UUID filialId;

    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "codigo_cupom", length = 40)
    private String codigoCupom;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desconto", nullable = false, length = 20)
    private DiscountType tipoDesconto;

    @Column(name = "valor_desconto", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorDesconto;

    @Column(name = "quantidade_minima", nullable = false, precision = 19, scale = 3)
    private BigDecimal quantidadeMinima;

    @Column(name = "inicio", nullable = false)
    private Instant inicio;

    @Column(name = "fim", nullable = false)
    private Instant fim;

    @Column(name = "prioridade", nullable = false)
    private int prioridade;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    protected PromotionEntity() {
    }

    public PromotionEntity(
            UUID empresaId,
            UUID filialId,
            UUID skuId,
            String nome,
            String codigoCupom,
            DiscountType tipoDesconto,
            BigDecimal valorDesconto,
            BigDecimal quantidadeMinima,
            Instant inicio,
            Instant fim,
            int prioridade) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.skuId = skuId;
        this.nome = nome;
        this.codigoCupom = codigoCupom;
        this.tipoDesconto = tipoDesconto;
        this.valorDesconto = valorDesconto;
        this.quantidadeMinima = quantidadeMinima;
        this.inicio = inicio;
        this.fim = fim;
        this.prioridade = prioridade;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
    }

    public UUID getSkuId() {
        return skuId;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public DiscountType getTipoDesconto() {
        return tipoDesconto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public BigDecimal getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getFim() {
        return fim;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
