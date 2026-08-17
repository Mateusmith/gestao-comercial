package br.com.commercecore.finance.internal;

import br.com.commercecore.finance.FinancialTitleStatus;
import br.com.commercecore.finance.TitleType;
import br.com.commercecore.shared.AbstractEntity;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.Dinheiro;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "titulo_financeiro")
public class FinancialTitleEntity extends AbstractEntity {

    @Column(name = "numero", nullable = false, unique = true, length = 40)
    private String numero;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "filial_id", nullable = false)
    private UUID filialId;

    @Column(name = "parceiro_id", nullable = false)
    private UUID parceiroId;

    @Column(name = "parceiro_nome", nullable = false, length = 160)
    private String parceiroNome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private TitleType tipo;

    @Column(name = "tipo_origem", nullable = false, length = 30)
    private String tipoOrigem;

    @Column(name = "origem_id", nullable = false)
    private UUID origemId;

    @Column(name = "documento_origem", nullable = false, length = 40)
    private String documentoOrigem;

    @Column(name = "parcela", nullable = false)
    private int parcela;

    @Column(name = "total_parcelas", nullable = false)
    private int totalParcelas;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "valor_original", precision = 19, scale = 2, nullable = false))
    private Dinheiro valorOriginal;

    @Embedded
    @AttributeOverride(name = "valor", column = @Column(name = "saldo", precision = 19, scale = 2, nullable = false))
    private Dinheiro saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FinancialTitleStatus status = FinancialTitleStatus.ABERTO;

    protected FinancialTitleEntity() {
    }

    public FinancialTitleEntity(
            String numero,
            UUID empresaId,
            UUID filialId,
            UUID parceiroId,
            String parceiroNome,
            TitleType tipo,
            String tipoOrigem,
            UUID origemId,
            String documentoOrigem,
            int parcela,
            int totalParcelas,
            LocalDate dataEmissao,
            LocalDate dataVencimento,
            Dinheiro valorOriginal) {
        this.numero = numero;
        this.empresaId = empresaId;
        this.filialId = filialId;
        this.parceiroId = parceiroId;
        this.parceiroNome = parceiroNome;
        this.tipo = tipo;
        this.tipoOrigem = tipoOrigem;
        this.origemId = origemId;
        this.documentoOrigem = documentoOrigem;
        this.parcela = parcela;
        this.totalParcelas = totalParcelas;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.valorOriginal = valorOriginal;
        this.saldo = valorOriginal;
    }

    public void liquidar(Dinheiro valor) {
        if (status == FinancialTitleStatus.QUITADO || status == FinancialTitleStatus.CANCELADO) {
            throw new BusinessRuleException("TITULO_NAO_LIQUIDAVEL", "O titulo nao esta aberto para liquidacao.");
        }
        if (!valor.positivo() || valor.compareTo(saldo) > 0) {
            throw new BusinessRuleException("VALOR_LIQUIDACAO_INVALIDO", "O valor deve ser positivo e nao pode superar o saldo.");
        }
        saldo = saldo.subtrair(valor);
        status = saldo.positivo() ? FinancialTitleStatus.PARCIAL : FinancialTitleStatus.QUITADO;
    }

    public void estornar(Dinheiro valor) {
        Dinheiro novoSaldo = saldo.somar(valor);
        if (novoSaldo.compareTo(valorOriginal) > 0) {
            throw new BusinessRuleException("ESTORNO_INVALIDO", "O estorno faria o saldo superar o valor original.");
        }
        saldo = novoSaldo;
        status = saldo.compareTo(valorOriginal) == 0 ? FinancialTitleStatus.ABERTO : FinancialTitleStatus.PARCIAL;
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

    public UUID getParceiroId() {
        return parceiroId;
    }

    public String getParceiroNome() {
        return parceiroNome;
    }

    public TitleType getTipo() {
        return tipo;
    }

    public String getTipoOrigem() {
        return tipoOrigem;
    }

    public UUID getOrigemId() {
        return origemId;
    }

    public String getDocumentoOrigem() {
        return documentoOrigem;
    }

    public int getParcela() {
        return parcela;
    }

    public int getTotalParcelas() {
        return totalParcelas;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public Dinheiro getValorOriginal() {
        return valorOriginal;
    }

    public Dinheiro getSaldo() {
        return saldo;
    }

    public FinancialTitleStatus getStatus() {
        return status;
    }
}
