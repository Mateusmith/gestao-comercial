package br.com.commercecore.finance.internal;

import br.com.commercecore.finance.SettlementKind;
import br.com.commercecore.finance.TitleType;
import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimentacao_financeira")
public class CashMovementEntity extends AbstractEntity {

    @Column(name = "conta_financeira_id", nullable = false)
    private UUID contaFinanceiraId;

    @Column(name = "liquidacao_id", nullable = false, unique = true)
    private UUID liquidacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", nullable = false, length = 10)
    private TitleType tipoTitulo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacao", nullable = false, length = 15)
    private SettlementKind tipoOperacao;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor", precision = 19, scale = 2, nullable = false))
    private Dinheiro valor;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "saldo_anterior", precision = 19, scale = 2, nullable = false))
    private Dinheiro saldoAnterior;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "saldo_posterior", precision = 19, scale = 2, nullable = false))
    private Dinheiro saldoPosterior;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected CashMovementEntity() {
    }

    public CashMovementEntity(
            UUID contaFinanceiraId,
            UUID liquidacaoId,
            TitleType tipoTitulo,
            SettlementKind tipoOperacao,
            Dinheiro valor,
            Dinheiro saldoAnterior,
            Dinheiro saldoPosterior,
            Instant ocorridoEm) {
        this.contaFinanceiraId = contaFinanceiraId;
        this.liquidacaoId = liquidacaoId;
        this.tipoTitulo = tipoTitulo;
        this.tipoOperacao = tipoOperacao;
        this.valor = valor;
        this.saldoAnterior = saldoAnterior;
        this.saldoPosterior = saldoPosterior;
        this.ocorridoEm = ocorridoEm;
    }
}
