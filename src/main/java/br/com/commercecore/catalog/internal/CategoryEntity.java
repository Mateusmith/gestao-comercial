package br.com.commercecore.catalog.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "categoria_produto", uniqueConstraints = @UniqueConstraint(name = "uq_categoria_empresa_nome", columnNames = {"empresa_id", "nome"}))
public class CategoryEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    protected CategoryEntity() {
    }

    public CategoryEntity(UUID empresaId, String nome) {
        this.empresaId = empresaId;
        this.nome = nome;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
