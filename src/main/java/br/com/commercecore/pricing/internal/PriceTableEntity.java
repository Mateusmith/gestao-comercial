package br.com.commercecore.pricing.internal;

import br.com.commercecore.shared.AbstractEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tabela_preco")
public class PriceTableEntity extends AbstractEntity {

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id")
    private UUID filialId;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "vigente_de", nullable = false)
    private Instant vigenteDe;

    @Column(name = "vigente_ate")
    private Instant vigenteAte;

    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    @OneToMany(mappedBy = "tabelaPreco", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("criadoEm ASC")
    private List<PriceTableItemEntity> itens = new ArrayList<>();

    protected PriceTableEntity() {
    }

    public PriceTableEntity(UUID empresaId, UUID filialId, String nome, Instant vigenteDe, Instant vigenteAte) {
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.nome = nome;
        this.vigenteDe = vigenteDe;
        this.vigenteAte = vigenteAte;
    }

    public void adicionarItem(PriceTableItemEntity item) {
        item.vincular(this);
        itens.add(item);
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getFilialId() {
        return filialId;
    }

    public String getNome() {
        return nome;
    }

    public Instant getVigenteDe() {
        return vigenteDe;
    }

    public Instant getVigenteAte() {
        return vigenteAte;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public List<PriceTableItemEntity> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
