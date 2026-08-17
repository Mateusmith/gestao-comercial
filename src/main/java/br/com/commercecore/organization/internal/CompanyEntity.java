package br.com.commercecore.organization.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "empresa")
public class CompanyEntity extends AbstractEntity {

    @Column(name = "razao_social", nullable = false, length = 160)
    private String razaoSocial;

    @Column(name = "nome_fantasia", nullable = false, length = 120)
    private String nomeFantasia;

    @Column(name = "cnpj", nullable = false, length = 14, unique = true)
    private String cnpj;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    protected CompanyEntity() {
    }

    public CompanyEntity(String razaoSocial, String nomeFantasia, String cnpj) {
        this.razaoSocial = razaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
