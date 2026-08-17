package br.com.commercecore.sales.internal;

import br.com.commercecore.sales.SalesOrderStatus;
import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido_venda")
public class SalesOrderEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "orcamento_id", nullable = false, unique = true)
    private UUID orcamentoId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "deposito_id", nullable = false)
    private UUID depositoId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "cliente_nome", nullable = false, length = 160)
    private String clienteNome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SalesOrderStatus status = SalesOrderStatus.RASCUNHO;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total = Dinheiro.zero();

    @Column(name = "numero_parcelas", nullable = false)
    private int numeroParcelas;

    @Column(name = "primeiro_vencimento", nullable = false)
    private LocalDate primeiroVencimento;

    @Column(name = "confirmado_em")
    private Instant confirmadoEm;

    @Column(name = "faturado_em")
    private Instant faturadoEm;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<SalesOrderItemEntity> itens = new ArrayList<>();

    protected SalesOrderEntity() {
    }

    public SalesOrderEntity(
            String numero,
            UUID orcamentoId,
            UUID empresaId,
            UUID filialId,
            UUID depositoId,
            UUID clienteId,
            String clienteNome,
            int numeroParcelas,
            LocalDate primeiroVencimento) {
        this.numero = numero;
        this.orcamentoId = orcamentoId;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.depositoId = depositoId;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.numeroParcelas = numeroParcelas;
        this.primeiroVencimento = primeiroVencimento;
    }

    public void adicionarItem(SalesOrderItemEntity item) {
        item.vincular(this);
        itens.add(item);
        total = total.somar(item.getSubtotal());
    }

    public void confirmar(Instant instante) {
        exigirStatus(SalesOrderStatus.RASCUNHO, "Somente um pedido em rascunho pode ser confirmado.");
        status = SalesOrderStatus.CONFIRMADO;
        confirmadoEm = instante;
    }

    public void faturar(Instant instante) {
        exigirStatus(SalesOrderStatus.CONFIRMADO, "Somente um pedido confirmado pode ser faturado.");
        status = SalesOrderStatus.FATURADO;
        faturadoEm = instante;
    }

    public void cancelar(Instant instante) {
        if (status == SalesOrderStatus.FATURADO || status == SalesOrderStatus.CANCELADO) {
            throw new BusinessRuleException(
                    "STATUS_PEDIDO_INVALIDO", "Um pedido faturado ou ja cancelado nao pode ser cancelado por este fluxo.");
        }
        status = SalesOrderStatus.CANCELADO;
        canceladoEm = instante;
    }

    private void exigirStatus(SalesOrderStatus esperado, String mensagem) {
        if (status != esperado) {
            throw new BusinessRuleException("STATUS_PEDIDO_INVALIDO", mensagem);
        }
    }

    public String getNumero() {
        return numero;
    }

    public UUID getOrcamentoId() {
        return orcamentoId;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
    }

    public UUID getDepositoId() {
        return depositoId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public SalesOrderStatus getStatus() {
        return status;
    }

    public Dinheiro getTotal() {
        return total;
    }

    public int getNumeroParcelas() {
        return numeroParcelas;
    }

    public LocalDate getPrimeiroVencimento() {
        return primeiroVencimento;
    }

    public Instant getConfirmadoEm() {
        return confirmadoEm;
    }

    public Instant getFaturadoEm() {
        return faturadoEm;
    }

    public Instant getCanceladoEm() {
        return canceladoEm;
    }

    public List<SalesOrderItemEntity> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
