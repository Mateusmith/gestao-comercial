package br.com.commercecore.purchasing.internal;

import br.com.commercecore.purchasing.RequisitionStatus;
import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "requisicao_compra")
public class PurchaseRequisitionEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "justificativa", nullable = false, length = 300)
    private String justificativa;

    @Column(name = "solicitada_por", nullable = false, length = 120)
    private String solicitadaPor;

    @Column(name = "aprovada_por", length = 120)
    private String aprovadaPor;

    @Column(name = "aprovada_em")
    private Instant aprovadaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RequisitionStatus status = RequisitionStatus.SOLICITADA;

    @OneToMany(mappedBy = "requisicao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<PurchaseRequisitionItemEntity> itens = new ArrayList<>();

    protected PurchaseRequisitionEntity() {
    }

    public PurchaseRequisitionEntity(
            String numero, UUID empresaId, UUID filialId, String justificativa, String solicitadaPor) {
        this.numero = numero;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.justificativa = justificativa;
        this.solicitadaPor = solicitadaPor;
    }

    public void adicionarItem(PurchaseRequisitionItemEntity item) {
        item.vincular(this);
        itens.add(item);
    }

    public void aprovar(String ator, Instant instante) {
        exigirStatus(RequisitionStatus.SOLICITADA, "Somente uma requisicao solicitada pode ser aprovada.");
        status = RequisitionStatus.APROVADA;
        aprovadaPor = ator;
        aprovadaEm = instante;
    }

    public void converter() {
        exigirStatus(RequisitionStatus.APROVADA, "Somente uma requisicao aprovada pode virar pedido.");
        status = RequisitionStatus.CONVERTIDA;
    }

    private void exigirStatus(RequisitionStatus esperado, String mensagem) {
        if (status != esperado) {
            throw new BusinessRuleException("STATUS_REQUISICAO_INVALIDO", mensagem);
        }
    }

    public String getNumero() { return numero; }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getFilialId() { return filialId; }
    public String getJustificativa() { return justificativa; }
    public String getSolicitadaPor() { return solicitadaPor; }
    public String getAprovadaPor() { return aprovadaPor; }
    public Instant getAprovadaEm() { return aprovadaEm; }
    public RequisitionStatus getStatus() { return status; }
    public List<PurchaseRequisitionItemEntity> getItens() { return Collections.unmodifiableList(itens); }
}
