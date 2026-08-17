package br.com.commercecore.sales.internal;

import br.com.commercecore.sales.QuoteStatus;
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
@Table(name = "orcamento_venda")
public class QuoteEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "cliente_nome", nullable = false, length = 160)
    private String clienteNome;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.EM_EDICAO;

    @Column(name = "valido_ate", nullable = false)
    private Instant validoAte;

    @Column(name = "codigo_cupom", length = 40)
    private String codigoCupom;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total = Dinheiro.zero();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<QuoteItemEntity> itens = new ArrayList<>();

    protected QuoteEntity() {
    }

    public QuoteEntity(
            String numero,
            UUID empresaId,
            UUID filialId,
            UUID clienteId,
            String clienteNome,
            Instant validoAte,
            String codigoCupom) {
        this.numero = numero;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.clienteNome = clienteNome;
        this.validoAte = validoAte;
        this.codigoCupom = codigoCupom;
    }

    public void adicionarItem(QuoteItemEntity item) {
        item.vincular(this);
        itens.add(item);
        total = total.somar(item.getSubtotal());
    }

    public void enviar() {
        exigirStatus(QuoteStatus.EM_EDICAO, "Somente um orcamento em edicao pode ser enviado.");
        status = QuoteStatus.ENVIADO;
    }

    public void aceitar(Instant agora) {
        exigirStatus(QuoteStatus.ENVIADO, "Somente um orcamento enviado pode ser aceito.");
        if (!validoAte.isAfter(agora)) {
            status = QuoteStatus.EXPIRADO;
            throw new BusinessRuleException("ORCAMENTO_EXPIRADO", "O prazo de validade do orcamento terminou.");
        }
        status = QuoteStatus.ACEITO;
    }

    public void converter() {
        exigirStatus(QuoteStatus.ACEITO, "Somente um orcamento aceito pode virar pedido.");
        status = QuoteStatus.CONVERTIDO;
    }

    private void exigirStatus(QuoteStatus esperado, String mensagem) {
        if (status != esperado) {
            throw new BusinessRuleException("STATUS_ORCAMENTO_INVALIDO", mensagem);
        }
    }

    public String getNumero() {
        return numero;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public QuoteStatus getStatus() {
        return status;
    }

    public Instant getValidoAte() {
        return validoAte;
    }

    public String getCodigoCupom() {
        return codigoCupom;
    }

    public Dinheiro getTotal() {
        return total;
    }

    public List<QuoteItemEntity> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
