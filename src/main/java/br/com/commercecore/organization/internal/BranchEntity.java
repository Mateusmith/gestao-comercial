package br.com.commercecore.organization.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@Table(name = "filial", uniqueConstraints = @UniqueConstraint(name = "uq_filial_empresa_codigo", columnNames = {"empresa_id", "codigo"}))
public class BranchEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "cnpj", nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(name = "fuso_horario", nullable = false, length = 60)
    private String fusoHorario;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    protected BranchEntity() {
    }

    public BranchEntity(UUID empresaId, String codigo, String nome, String cnpj, String fusoHorario) {
        this.empresaId = empresaId;
        this.codigo = codigo;
        this.nome = nome;
        this.cnpj = cnpj;
        this.fusoHorario = fusoHorario;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
