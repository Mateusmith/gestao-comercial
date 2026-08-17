package br.com.commercecore.purchasing.internal;

import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recebimento_compra")
public class PurchaseReceiptEntity extends AbstractEntity {
    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String numero;
    @Column(name = "pedido_id", nullable = false)
    private UUID pedidoId;
    @Column(name = "documento_fornecedor", nullable = false, length = 50)
    private String documentoFornecedor;
    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_total", precision = 19, scale = 2, nullable = false))
    private Dinheiro total = Dinheiro.zero();
    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;
    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;
    @OneToMany(mappedBy = "recebimento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PurchaseReceiptItemEntity> itens = new ArrayList<>();

    protected PurchaseReceiptEntity() {
    }

    public PurchaseReceiptEntity(
            String numero, UUID pedidoId, String documentoFornecedor, Instant recebidoEm, String chaveIdempotencia) {
        this.numero = numero;
        this.pedidoId = pedidoId;
        this.documentoFornecedor = documentoFornecedor;
        this.recebidoEm = recebidoEm;
        this.chaveIdempotencia = chaveIdempotencia;
    }

    public void adicionarItem(PurchaseReceiptItemEntity item) {
        item.vincular(this);
        itens.add(item);
        total = total.somar(item.getSubtotal());
    }

    public String getNumero() { return numero; }
    public UUID getPedidoId() { return pedidoId; }
    public String getDocumentoFornecedor() { return documentoFornecedor; }
    public Dinheiro getTotal() { return total; }
    public Instant getRecebidoEm() { return recebidoEm; }
    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public List<PurchaseReceiptItemEntity> getItens() { return Collections.unmodifiableList(itens); }
}
