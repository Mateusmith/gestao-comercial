package br.com.commercecore.inventory.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "deposito")
public class WarehouseEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    protected WarehouseEntity() {
    }

    public WarehouseEntity(UUID empresaId, UUID filialId, String codigo, String nome) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.codigo = codigo;
        this.nome = nome;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
