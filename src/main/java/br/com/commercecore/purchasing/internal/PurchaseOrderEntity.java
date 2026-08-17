package br.com.commercecore.purchasing.internal;

import br.com.commercecore.purchasing.PurchaseOrderStatus;
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
@Table(name = "pedido_compra")
public class PurchaseOrderEntity extends AbstractEntity {
    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;
    @Column(name = "requisicao_id", nullable = false, unique = true)
    private UUID requisicaoId;
    @Column(name = "cotacao_id", nullable = false, unique = true)
    private UUID cotacaoId;
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(name = "deposito_id", nullable = false)
    private UUID depositoId;
    @Column(name = "fornecedor_id", nullable = false)
    private UUID fornecedorId;
    @Column(name = "fornecedor_nome", nullable = false, length = 160)
    private String fornecedorNome;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private PurchaseOrderStatus status = PurchaseOrderStatus.EMITIDO;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total = Dinheiro.zero();
    @Column(name = "numero_parcelas", nullable = false)
    private int numeroParcelas;
    @Column(name = "primeiro_vencimento", nullable = false)
    private LocalDate primeiroVencimento;
    @Column(name = "emitido_em", nullable = false)
    private Instant emitidoEm;
    @Column(name = "concluido_em")
    private Instant concluidoEm;
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<PurchaseOrderItemEntity> itens = new ArrayList<>();

    protected PurchaseOrderEntity() {
    }

    public PurchaseOrderEntity(
            String numero, UUID requisicaoId, UUID cotacaoId, UUID empresaId, UUID filialId,
            UUID depositoId, UUID fornecedorId, String fornecedorNome, int numeroParcelas,
            LocalDate primeiroVencimento, Instant emitidoEm) {
        this.numero = numero;
        this.requisicaoId = requisicaoId;
        this.cotacaoId = cotacaoId;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.depositoId = depositoId;
        this.fornecedorId = fornecedorId;
        this.fornecedorNome = fornecedorNome;
        this.numeroParcelas = numeroParcelas;
        this.primeiroVencimento = primeiroVencimento;
        this.emitidoEm = emitidoEm;
    }

    public void adicionarItem(PurchaseOrderItemEntity item) {
        item.vincular(this);
        itens.add(item);
        total = total.somar(item.getSubtotal());
    }

    public void atualizarAposRecebimento(Instant instante) {
        if (status == PurchaseOrderStatus.CANCELADO || status == PurchaseOrderStatus.RECEBIDO) {
            throw new BusinessRuleException("STATUS_PEDIDO_COMPRA_INVALIDO", "O pedido nao aceita recebimento.");
        }
        boolean completo = itens.stream().allMatch(PurchaseOrderItemEntity::completamenteRecebido);
        status = completo ? PurchaseOrderStatus.RECEBIDO : PurchaseOrderStatus.PARCIALMENTE_RECEBIDO;
        concluidoEm = completo ? instante : null;
    }

    public String getNumero() { return numero; }
    public UUID getRequisicaoId() { return requisicaoId; }
    public UUID getCotacaoId() { return cotacaoId; }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getFilialId() { return filialId; }
    public UUID getDepositoId() { return depositoId; }
    public UUID getFornecedorId() { return fornecedorId; }
    public String getFornecedorNome() { return fornecedorNome; }
    public PurchaseOrderStatus getStatus() { return status; }
    public Dinheiro getTotal() { return total; }
    public int getNumeroParcelas() { return numeroParcelas; }
    public LocalDate getPrimeiroVencimento() { return primeiroVencimento; }
    public Instant getEmitidoEm() { return emitidoEm; }
    public Instant getConcluidoEm() { return concluidoEm; }
    public List<PurchaseOrderItemEntity> getItens() { return Collections.unmodifiableList(itens); }
}
