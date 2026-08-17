package br.com.commercecore.partner.internal;

import br.com.commercecore.partner.PartnerRole;
import br.com.commercecore.partner.PersonType;
import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "parceiro_comercial", uniqueConstraints = @UniqueConstraint(name = "uq_parceiro_empresa_documento", columnNames = {"empresa_id", "cpf_cnpj"}))
public class PartnerEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private PersonType tipoPessoa;

    @Column(name = "nome_razao_social", nullable = false, length = 160)
    private String nomeRazaoSocial;

    @Column(name = "nome_fantasia", length = 120)
    private String nomeFantasia;

    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "papel_parceiro", joinColumns = @JoinColumn(name = "parceiro_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 30)
    private Set<PartnerRole> papeis = new LinkedHashSet<>();

    protected PartnerEntity() {
    }

    public PartnerEntity(
            UUID empresaId,
            PersonType tipoPessoa,
            String nomeRazaoSocial,
            String nomeFantasia,
            String cpfCnpj,
            String email,
            String telefone,
            Set<PartnerRole> papeis) {
        this.empresaId = empresaId;
        this.tipoPessoa = tipoPessoa;
        this.nomeRazaoSocial = nomeRazaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
        this.papeis.addAll(papeis);
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public PersonType getTipoPessoa() {
        return tipoPessoa;
    }

    public String getNomeRazaoSocial() {
        return nomeRazaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<PartnerRole> getPapeis() {
        return Set.copyOf(papeis);
    }

    public boolean possuiPapel(PartnerRole papel) {
        return papeis.contains(papel);
    }
}
