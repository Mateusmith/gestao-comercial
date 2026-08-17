package br.com.commercecore.purchasing.internal;

import br.com.commercecore.purchasing.SupplierQuoteStatus;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cotacao_fornecedor")
public class SupplierQuoteEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;
    @Column(name = "requisicao_id", nullable = false)
    private UUID requisicaoId;
    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;
    @Column(name = "filial_id", nullable = false)
    private UUID filialId;
    @Column(name = "fornecedor_id", nullable = false)
    private UUID fornecedorId;
    @Column(name = "fornecedor_nome", nullable = false, length = 160)
    private String fornecedorNome;
    @Column(name = "valido_ate", nullable = false)
    private Instant validoAte;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupplierQuoteStatus status = SupplierQuoteStatus.RECEBIDA;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total = Dinheiro.zero();
    @OneToMany(mappedBy = "cotacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<SupplierQuoteItemEntity> itens = new ArrayList<>();

    protected SupplierQuoteEntity() {
    }

    public SupplierQuoteEntity(
            String numero, UUID requisicaoId, UUID empresaId, UUID filialId,
            UUID fornecedorId, String fornecedorNome, Instant validoAte) {
        this.numero = numero;
        this.requisicaoId = requisicaoId;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.fornecedorId = fornecedorId;
        this.fornecedorNome = fornecedorNome;
        this.validoAte = validoAte;
    }

    public void adicionarItem(SupplierQuoteItemEntity item) {
        item.vincular(this);
        itens.add(item);
        total = total.somar(item.getSubtotal());
    }

    public void selecionar(Instant agora) {
        if (status != SupplierQuoteStatus.RECEBIDA) {
            throw new BusinessRuleException("STATUS_COTACAO_INVALIDO", "Somente uma cotacao recebida pode ser selecionada.");
        }
        if (!validoAte.isAfter(agora)) {
            status = SupplierQuoteStatus.EXPIRADA;
            throw new BusinessRuleException("COTACAO_EXPIRADA", "A cotacao do fornecedor expirou.");
        }
        status = SupplierQuoteStatus.SELECIONADA;
    }

    public String getNumero() { return numero; }
    public UUID getRequisicaoId() { return requisicaoId; }
    public UUID getEmpresaId() { return empresaId; }
    public UUID getFilialId() { return filialId; }
    public UUID getFornecedorId() { return fornecedorId; }
    public String getFornecedorNome() { return fornecedorNome; }
    public Instant getValidoAte() { return validoAte; }
    public SupplierQuoteStatus getStatus() { return status; }
    public Dinheiro getTotal() { return total; }
    public List<SupplierQuoteItemEntity> getItens() { return Collections.unmodifiableList(itens); }
}
