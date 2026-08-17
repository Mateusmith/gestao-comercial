package br.com.commercecore.finance.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "conta_financeira")
public class FinancialAccountEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "codigo", nullable = false, length = 30)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "saldo", precision = 19, scale = 2, nullable = false))
    private Dinheiro saldo;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    protected FinancialAccountEntity() {
    }

    public FinancialAccountEntity(UUID empresaId, UUID filialId, String codigo, String nome, Dinheiro saldoInicial) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.codigo = codigo;
        this.nome = nome;
        this.saldo = saldoInicial;
    }

    public void creditar(Dinheiro valor) {
        saldo = saldo.somar(valor);
    }

    public void debitar(Dinheiro valor) {
        Dinheiro novoSaldo = saldo.subtrair(valor);
        if (novoSaldo.negativo()) {
            throw new BusinessRuleException("SALDO_FINANCEIRO_INSUFICIENTE", "A conta financeira nao possui saldo suficiente.");
        }
        saldo = novoSaldo;
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

    public Dinheiro getSaldo() {
        return saldo;
    }

    public boolean isAtiva() {
        return ativa;
    }
}
