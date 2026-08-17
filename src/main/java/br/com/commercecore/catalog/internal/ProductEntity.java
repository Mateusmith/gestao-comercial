package br.com.commercecore.catalog.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "produto", uniqueConstraints = @UniqueConstraint(name = "uq_produto_empresa_codigo", columnNames = {"empresa_id", "codigo"}))
public class ProductEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SkuEntity> skus = new ArrayList<>();

    protected ProductEntity() {
    }

    public ProductEntity(UUID empresaId, UUID categoriaId, String codigo, String nome, String descricao) {
        this.empresaId = empresaId;
        this.categoriaId = categoriaId;
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }

    public void adicionarSku(SkuEntity sku) {
        sku.vincular(this);
        skus.add(sku);
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public List<SkuEntity> getSkus() {
        return List.copyOf(skus);
    }
}
