package br.com.commercecore.finance.internal;

import br.com.commercecore.finance.PaymentMethod;
import br.com.commercecore.finance.SettlementKind;
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
@Table(name = "liquidacao_financeira")
public class SettlementEntity extends AbstractEntity {

    @Column(name = "titulo_id", nullable = false)
    private UUID tituloId;

    @Column(name = "conta_financeira_id", nullable = false)
    private UUID contaFinanceiraId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 15)
    private SettlementKind tipo;

    @Column(name = "liquidacao_original_id")
    private UUID liquidacaoOriginalId;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor", precision = 19, scale = 2, nullable = false))
    private Dinheiro valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private PaymentMethod formaPagamento;

    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 100)
    private String chaveIdempotencia;

    @Column(name = "observacao", length = 300)
    private String observacao;

    @Column(name = "realizado_por", nullable = false, length = 120)
    private String realizadoPor;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected SettlementEntity() {
    }

    public SettlementEntity(
            UUID tituloId,
            UUID contaFinanceiraId,
            SettlementKind tipo,
            UUID liquidacaoOriginalId,
            Dinheiro valor,
            PaymentMethod formaPagamento,
            String chaveIdempotencia,
            String observacao,
            String realizadoPor,
            Instant ocorridoEm) {
        this.tituloId = tituloId;
        this.contaFinanceiraId = contaFinanceiraId;
        this.tipo = tipo;
        this.liquidacaoOriginalId = liquidacaoOriginalId;
        this.valor = valor;
        this.formaPagamento = formaPagamento;
        this.chaveIdempotencia = chaveIdempotencia;
        this.observacao = observacao;
        this.realizadoPor = realizadoPor;
        this.ocorridoEm = ocorridoEm;
    }

    public UUID getTituloId() {
        return tituloId;
    }

    public UUID getContaFinanceiraId() {
        return contaFinanceiraId;
    }

    public SettlementKind getTipo() {
        return tipo;
    }

    public UUID getLiquidacaoOriginalId() {
        return liquidacaoOriginalId;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public PaymentMethod getFormaPagamento() {
        return formaPagamento;
    }

    public String getChaveIdempotencia() {
        return chaveIdempotencia;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getRealizadoPor() {
        return realizadoPor;
    }

    public Instant getOcorridoEm() {
        return ocorridoEm;
    }
}
