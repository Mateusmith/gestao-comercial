package br.com.commercecore.finance;

import br.com.commercecore.finance.internal.CashMovementEntity;
import br.com.commercecore.finance.internal.CashMovementRepository;
import br.com.commercecore.finance.internal.FinancialAccountEntity;
import br.com.commercecore.finance.internal.FinancialAccountRepository;
import br.com.commercecore.finance.internal.FinancialTitleEntity;
import br.com.commercecore.finance.internal.FinancialTitleRepository;
import br.com.commercecore.finance.internal.ProcessedFinancialEventEntity;
import br.com.commercecore.finance.internal.ProcessedFinancialEventRepository;
import br.com.commercecore.finance.internal.SettlementEntity;
import br.com.commercecore.finance.internal.SettlementRepository;
import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.sales.SaleInvoicedEvent;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.ConflictException;
import br.com.commercecore.shared.CurrentActor;
import br.com.commercecore.shared.Dinheiro;
import br.com.commercecore.shared.NotFoundException;
import br.com.commercecore.shared.PageResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceService {

    private final FinancialAccountRepository contas;
    private final FinancialTitleRepository titulos;
    private final SettlementRepository liquidacoes;
    private final CashMovementRepository movimentos;
    private final ProcessedFinancialEventRepository eventosProcessados;
    private final BranchAccessService acessoFilial;
    private final CurrentActor atorAtual;
    private final Clock relogio;

    public FinanceService(
            FinancialAccountRepository contas,
            FinancialTitleRepository titulos,
            SettlementRepository liquidacoes,
            CashMovementRepository movimentos,
            ProcessedFinancialEventRepository eventosProcessados,
            BranchAccessService acessoFilial,
            CurrentActor atorAtual,
            Clock relogio) {
        this.contas = contas;
        this.titulos = titulos;
        this.liquidacoes = liquidacoes;
        this.movimentos = movimentos;
        this.eventosProcessados = eventosProcessados;
        this.acessoFilial = acessoFilial;
        this.atorAtual = atorAtual;
        this.relogio = relogio;
    }

    @Transactional
    public FinancialAccountResponse criarConta(CreateFinancialAccountRequest requisicao) {
        var filial = acessoFilial.garantirAcesso(requisicao.filialId());
        if (!filial.empresaId().equals(requisicao.empresaId())) {
            throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
        }
        String codigo = requisicao.codigo().trim().toUpperCase();
        if (contas.existsByFilialIdAndCodigo(requisicao.filialId(), codigo)) {
            throw new BusinessRuleException("CONTA_DUPLICADA", "Ja existe uma conta financeira com este codigo.");
        }
        FinancialAccountEntity conta = contas.save(new FinancialAccountEntity(
                requisicao.empresaId(), requisicao.filialId(), codigo, requisicao.nome().trim(),
                Dinheiro.de(requisicao.saldoInicial())));
        return resposta(conta);
    }

    @Transactional
    public void criarRecebiveis(SaleInvoicedEvent evento) {
        criarTitulos(
                evento.eventoId(), "VENDA_FATURADA", evento.faturaId(), evento.numeroFatura(), evento.empresaId(),
                evento.filialId(), evento.clienteId(), evento.clienteNome(), evento.valorTotal(),
                evento.numeroParcelas(), evento.primeiroVencimento(), evento.faturadoEm(), TitleType.RECEBER);
    }

    @Transactional
    public void criarContasPagar(PayableCreatedCommand comando) {
        criarTitulos(
                comando.eventoId(), "COMPRA_RECEBIDA", comando.origemId(), comando.documentoOrigem(),
                comando.empresaId(), comando.filialId(), comando.fornecedorId(), comando.fornecedorNome(),
                comando.valorTotal(), comando.numeroParcelas(), comando.primeiroVencimento(), comando.ocorridoEm(),
                TitleType.PAGAR);
    }

    @Transactional
    public SettlementResponse liquidar(UUID tituloId, String chaveIdempotencia, SettleTitleRequest requisicao) {
        validarChaveIdempotencia(chaveIdempotencia);
        var repetida = liquidacoes.findByChaveIdempotencia(chaveIdempotencia);
        if (repetida.isPresent()) {
            SettlementEntity liquidacao = repetida.get();
            if (!liquidacao.getTituloId().equals(tituloId)
                    || liquidacao.getValor().compareTo(Dinheiro.de(requisicao.valor())) != 0) {
                throw new ConflictException(
                        "IDEMPOTENCIA_DIVERGENTE", "A chave ja foi usada em uma liquidacao com outros dados.");
            }
            return montarResposta(liquidacao);
        }

        FinancialTitleEntity titulo = obterTituloBloqueado(tituloId);
        acessoFilial.garantirAcesso(titulo.getFilialId());
        FinancialAccountEntity conta = obterContaBloqueada(requisicao.contaFinanceiraId());
        validarContaDoTitulo(conta, titulo);
        Dinheiro valor = Dinheiro.de(requisicao.valor());
        Dinheiro saldoContaAnterior = conta.getSaldo();
        titulo.liquidar(valor);
        aplicarNaConta(conta, titulo.getTipo(), SettlementKind.LIQUIDACAO, valor);
        Instant ocorridoEm = requisicao.ocorridoEm() == null ? relogio.instant() : requisicao.ocorridoEm();
        SettlementEntity liquidacao = liquidacoes.save(new SettlementEntity(
                titulo.getId(), conta.getId(), SettlementKind.LIQUIDACAO, null, valor,
                requisicao.formaPagamento(), chaveIdempotencia.trim(), normalizar(requisicao.observacao()),
                atorAtual.id(), ocorridoEm));
        movimentos.save(new CashMovementEntity(
                conta.getId(), liquidacao.getId(), titulo.getTipo(), SettlementKind.LIQUIDACAO, valor,
                saldoContaAnterior, conta.getSaldo(), ocorridoEm));
        return resposta(liquidacao, titulo, conta);
    }

    @Transactional
    public SettlementResponse estornar(
            UUID liquidacaoId, String chaveIdempotencia, ReverseSettlementRequest requisicao) {
        validarChaveIdempotencia(chaveIdempotencia);
        var repetida = liquidacoes.findByChaveIdempotencia(chaveIdempotencia);
        if (repetida.isPresent()) {
            return montarResposta(repetida.get());
        }
        SettlementEntity original = liquidacoes.findById(liquidacaoId)
                .orElseThrow(() -> new NotFoundException("Liquidacao financeira nao encontrada."));
        if (original.getTipo() != SettlementKind.LIQUIDACAO
                || liquidacoes.existsByLiquidacaoOriginalIdAndTipo(liquidacaoId, SettlementKind.ESTORNO)) {
            throw new BusinessRuleException("LIQUIDACAO_NAO_ESTORNAVEL", "A liquidacao ja foi estornada ou nao e original.");
        }
        FinancialTitleEntity titulo = obterTituloBloqueado(original.getTituloId());
        acessoFilial.garantirAcesso(titulo.getFilialId());
        FinancialAccountEntity conta = obterContaBloqueada(original.getContaFinanceiraId());
        Dinheiro saldoContaAnterior = conta.getSaldo();
        titulo.estornar(original.getValor());
        aplicarNaConta(conta, titulo.getTipo(), SettlementKind.ESTORNO, original.getValor());
        Instant agora = relogio.instant();
        SettlementEntity estorno = liquidacoes.save(new SettlementEntity(
                titulo.getId(), conta.getId(), SettlementKind.ESTORNO, original.getId(), original.getValor(),
                original.getFormaPagamento(), chaveIdempotencia.trim(), requisicao.motivo().trim(),
                atorAtual.id(), agora));
        movimentos.save(new CashMovementEntity(
                conta.getId(), estorno.getId(), titulo.getTipo(), SettlementKind.ESTORNO, original.getValor(),
                saldoContaAnterior, conta.getSaldo(), agora));
        return resposta(estorno, titulo, conta);
    }

    @Transactional(readOnly = true)
    public PageResponse<FinancialTitleResponse> listarTitulos(
            UUID empresaId,
            TitleType tipo,
            Collection<FinancialTitleStatus> status,
            int pagina,
            int tamanho) {
        var paginacao = PageRequest.of(
                pagina, Math.min(tamanho, 100), Sort.by("dataVencimento").ascending().and(Sort.by("numero")));
        Collection<FinancialTitleStatus> filtro = status == null || status.isEmpty()
                ? List.of(FinancialTitleStatus.ABERTO, FinancialTitleStatus.PARCIAL, FinancialTitleStatus.QUITADO)
                : status;
        return PageResponse.de(titulos.findByEmpresaIdAndTipoAndStatusIn(empresaId, tipo, filtro, paginacao)
                .map(this::resposta));
    }

    @Transactional(readOnly = true)
    public FinancialTitleResponse obterTitulo(UUID id) {
        FinancialTitleEntity titulo = titulos.findById(id)
                .orElseThrow(() -> new NotFoundException("Titulo financeiro nao encontrado."));
        acessoFilial.garantirAcesso(titulo.getFilialId());
        return resposta(titulo);
    }

    private void criarTitulos(
            UUID eventoId,
            String tipoEvento,
            UUID origemId,
            String documentoOrigem,
            UUID empresaId,
            UUID filialId,
            UUID parceiroId,
            String parceiroNome,
            BigDecimal valorTotal,
            int numeroParcelas,
            LocalDate primeiroVencimento,
            Instant ocorridoEm,
            TitleType tipo) {
        if (eventosProcessados.existsByEventoId(eventoId)) {
            return;
        }
        if (numeroParcelas < 1 || numeroParcelas > 24) {
            throw new BusinessRuleException("PARCELAMENTO_INVALIDO", "O parcelamento deve ter entre 1 e 24 parcelas.");
        }
        List<Dinheiro> valores = dividir(valorTotal, numeroParcelas);
        LocalDate emissao = LocalDate.ofInstant(ocorridoEm, ZoneId.of("UTC"));
        for (int indice = 0; indice < numeroParcelas; indice++) {
            int parcela = indice + 1;
            if (titulos.existsByTipoOrigemAndOrigemIdAndParcela(tipoEvento, origemId, parcela)) {
                continue;
            }
            titulos.save(new FinancialTitleEntity(
                    numero(tipo, titulos.proximoNumero()), empresaId, filialId, parceiroId, parceiroNome, tipo,
                    tipoEvento, origemId, documentoOrigem, parcela, numeroParcelas, emissao,
                    primeiroVencimento.plusMonths(indice), valores.get(indice)));
        }
        eventosProcessados.save(new ProcessedFinancialEventEntity(eventoId, tipoEvento, relogio.instant()));
    }

    static List<Dinheiro> dividir(BigDecimal valorTotal, int parcelas) {
        BigDecimal total = valorTotal.setScale(2, RoundingMode.UNNECESSARY);
        long centavos = total.movePointRight(2).longValueExact();
        if (centavos < parcelas) {
            throw new BusinessRuleException(
                    "PARCELAMENTO_SUPERIOR_AO_VALOR",
                    "O valor total deve permitir parcelas de no minimo um centavo.");
        }
        long base = centavos / parcelas;
        long resto = centavos % parcelas;
        List<Dinheiro> valores = new ArrayList<>(parcelas);
        for (int indice = 0; indice < parcelas; indice++) {
            long valor = base + (indice < resto ? 1 : 0);
            valores.add(Dinheiro.de(BigDecimal.valueOf(valor, 2)));
        }
        return valores;
    }

    private void aplicarNaConta(
            FinancialAccountEntity conta, TitleType tipoTitulo, SettlementKind operacao, Dinheiro valor) {
        boolean entrada = (tipoTitulo == TitleType.RECEBER && operacao == SettlementKind.LIQUIDACAO)
                || (tipoTitulo == TitleType.PAGAR && operacao == SettlementKind.ESTORNO);
        if (entrada) {
            conta.creditar(valor);
        } else {
            conta.debitar(valor);
        }
    }

    private FinancialTitleEntity obterTituloBloqueado(UUID id) {
        return titulos.buscarComBloqueio(id)
                .orElseThrow(() -> new NotFoundException("Titulo financeiro nao encontrado."));
    }

    private FinancialAccountEntity obterContaBloqueada(UUID id) {
        return contas.findWithLockById(id)
                .orElseThrow(() -> new NotFoundException("Conta financeira nao encontrada."));
    }

    private void validarContaDoTitulo(FinancialAccountEntity conta, FinancialTitleEntity titulo) {
        if (!conta.isAtiva()
                || !conta.getEmpresaId().equals(titulo.getEmpresaId())
                || !conta.getFilialId().equals(titulo.getFilialId())) {
            throw new BusinessRuleException(
                    "CONTA_FINANCEIRA_INVALIDA", "A conta deve estar ativa e pertencer a empresa e filial do titulo.");
        }
    }

    private SettlementResponse montarResposta(SettlementEntity liquidacao) {
        FinancialTitleEntity titulo = titulos.findById(liquidacao.getTituloId())
                .orElseThrow(() -> new NotFoundException("Titulo financeiro nao encontrado."));
        FinancialAccountEntity conta = contas.findById(liquidacao.getContaFinanceiraId())
                .orElseThrow(() -> new NotFoundException("Conta financeira nao encontrada."));
        return resposta(liquidacao, titulo, conta);
    }

    private FinancialTitleResponse resposta(FinancialTitleEntity titulo) {
        FinancialTitleStatus status = titulo.getStatus();
        if ((status == FinancialTitleStatus.ABERTO || status == FinancialTitleStatus.PARCIAL)
                && titulo.getDataVencimento().isBefore(LocalDate.now(relogio))) {
            status = FinancialTitleStatus.VENCIDO;
        }
        return new FinancialTitleResponse(
                titulo.getId(), titulo.getNumero(), titulo.getEmpresaId(), titulo.getFilialId(),
                titulo.getParceiroId(), titulo.getParceiroNome(), titulo.getTipo(), titulo.getTipoOrigem(),
                titulo.getOrigemId(), titulo.getDocumentoOrigem(), titulo.getParcela(), titulo.getTotalParcelas(),
                titulo.getDataEmissao(), titulo.getDataVencimento(), titulo.getValorOriginal(), titulo.getSaldo(),
                status, titulo.getVersao());
    }

    private static FinancialAccountResponse resposta(FinancialAccountEntity conta) {
        return new FinancialAccountResponse(
                conta.getId(), conta.getEmpresaId(), conta.getFilialId(), conta.getCodigo(), conta.getNome(),
                conta.getSaldo(), conta.isAtiva(), conta.getVersao());
    }

    private static SettlementResponse resposta(
            SettlementEntity liquidacao, FinancialTitleEntity titulo, FinancialAccountEntity conta) {
        return new SettlementResponse(
                liquidacao.getId(), liquidacao.getTituloId(), liquidacao.getContaFinanceiraId(), liquidacao.getTipo(),
                liquidacao.getLiquidacaoOriginalId(), liquidacao.getValor(), liquidacao.getFormaPagamento(),
                liquidacao.getChaveIdempotencia(), liquidacao.getObservacao(), liquidacao.getRealizadoPor(),
                liquidacao.getOcorridoEm(), titulo.getSaldo(), conta.getSaldo());
    }

    private static String numero(TitleType tipo, long sequencia) {
        return "%s-%08d".formatted(tipo == TitleType.RECEBER ? "CR" : "CP", sequencia);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static void validarChaveIdempotencia(String chave) {
        if (chave == null || chave.isBlank() || chave.length() > 100) {
            throw new BusinessRuleException(
                    "CHAVE_IDEMPOTENCIA_INVALIDA", "O cabecalho Idempotency-Key e obrigatorio e aceita ate 100 caracteres.");
        }
    }
}
