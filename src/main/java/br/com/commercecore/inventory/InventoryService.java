package br.com.commercecore.inventory;

import br.com.commercecore.catalog.CatalogService;
import br.com.commercecore.catalog.SkuSnapshot;
import br.com.commercecore.inventory.internal.StockBalanceEntity;
import br.com.commercecore.inventory.internal.StockBalanceRepository;
import br.com.commercecore.inventory.internal.StockLockRepository;
import br.com.commercecore.inventory.internal.StockMovementEntity;
import br.com.commercecore.inventory.internal.StockMovementRepository;
import br.com.commercecore.inventory.internal.StockReservationEntity;
import br.com.commercecore.inventory.internal.StockReservationRepository;
import br.com.commercecore.inventory.internal.WarehouseEntity;
import br.com.commercecore.inventory.internal.WarehouseRepository;
import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.CurrentActor;
import br.com.commercecore.shared.NotFoundException;
import br.com.commercecore.shared.PageResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private static final String SEM_LOTE = "SEM_LOTE";

    private final WarehouseRepository depositos;
    private final StockBalanceRepository saldos;
    private final StockReservationRepository reservas;
    private final StockMovementRepository movimentacoes;
    private final StockLockRepository bloqueios;
    private final CatalogService catalogo;
    private final BranchAccessService acessoFilial;
    private final CurrentActor atorAtual;
    private final Clock relogio;

    public InventoryService(
            WarehouseRepository depositos,
            StockBalanceRepository saldos,
            StockReservationRepository reservas,
            StockMovementRepository movimentacoes,
            StockLockRepository bloqueios,
            CatalogService catalogo,
            BranchAccessService acessoFilial,
            CurrentActor atorAtual,
            Clock relogio) {
        this.depositos = depositos;
        this.saldos = saldos;
        this.reservas = reservas;
        this.movimentacoes = movimentacoes;
        this.bloqueios = bloqueios;
        this.catalogo = catalogo;
        this.acessoFilial = acessoFilial;
        this.atorAtual = atorAtual;
        this.relogio = relogio;
    }

    @Transactional
    public WarehouseResponse criarDeposito(CreateWarehouseRequest requisicao) {
        var filial = acessoFilial.garantirAcesso(requisicao.filialId());
        if (!filial.empresaId().equals(requisicao.empresaId())) {
            throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
        }
        String codigo = requisicao.codigo().trim().toUpperCase();
        if (depositos.existsByFilialIdAndCodigo(requisicao.filialId(), codigo)) {
            throw new BusinessRuleException("DEPOSITO_DUPLICADO", "Ja existe um deposito com este codigo na filial.");
        }
        return resposta(depositos.save(new WarehouseEntity(
                requisicao.empresaId(), requisicao.filialId(), codigo, requisicao.nome().trim())));
    }

    @Transactional
    public StockBalanceResponse ajustar(AdjustStockRequest requisicao) {
        WarehouseEntity deposito = validarContexto(
                requisicao.empresaId(), requisicao.filialId(), requisicao.depositoId(), requisicao.skuId());
        SkuSnapshot sku = catalogo.obterSku(requisicao.skuId());
        BigDecimal quantidade = validarQuantidade(requisicao.quantidade(), sku);
        String lote = normalizarLote(requisicao.lote(), sku);
        validarValidade(requisicao.validadeLote());
        StockBalanceEntity saldo = obterSaldoBloqueado(
                deposito.getId(), requisicao.skuId(), lote, requisicao.validadeLote());
        BigDecimal fisicoAnterior = saldo.getSaldoFisico();
        BigDecimal reservadoAnterior = saldo.getSaldoReservado();
        MovementType tipo;
        if (requisicao.direcao() == AdjustmentDirection.ENTRADA) {
            saldo.entrar(quantidade);
            tipo = MovementType.AJUSTE_ENTRADA;
        } else {
            saldo.sair(quantidade);
            tipo = MovementType.AJUSTE_SAIDA;
        }
        saldos.save(saldo);
        registrarMovimento(
                requisicao.empresaId(), requisicao.filialId(), saldo, tipo, quantidade,
                fisicoAnterior, reservadoAnterior, StockOriginType.AJUSTE, UUID.randomUUID(),
                requisicao.justificativa().trim());
        return resposta(saldo);
    }

    @Transactional
    public List<StockBalanceResponse> transferir(TransferStockRequest requisicao) {
        if (requisicao.depositoOrigemId().equals(requisicao.depositoDestinoId())) {
            throw new BusinessRuleException("DEPOSITOS_IGUAIS", "Os depositos de origem e destino devem ser diferentes.");
        }
        WarehouseEntity origem = validarContexto(
                requisicao.empresaId(), requisicao.filialId(), requisicao.depositoOrigemId(), requisicao.skuId());
        WarehouseEntity destino = validarContexto(
                requisicao.empresaId(), requisicao.filialId(), requisicao.depositoDestinoId(), requisicao.skuId());
        SkuSnapshot sku = catalogo.obterSku(requisicao.skuId());
        BigDecimal quantidade = validarQuantidade(requisicao.quantidade(), sku);
        String lote = normalizarLote(requisicao.lote(), sku);
        UUID transferenciaId = UUID.randomUUID();

        List<UUID> ordem = List.of(origem.getId(), destino.getId()).stream().sorted().toList();
        ordem.forEach(id -> travar(id, requisicao.skuId()));
        StockBalanceEntity saldoOrigem = saldos.findByDepositoIdAndSkuIdAndLote(origem.getId(), sku.id(), lote)
                .orElseThrow(() -> new BusinessRuleException("SALDO_INEXISTENTE", "Nao existe saldo do lote na origem."));
        StockBalanceEntity saldoDestino = saldos.findByDepositoIdAndSkuIdAndLote(destino.getId(), sku.id(), lote)
                .orElseGet(() -> saldos.save(new StockBalanceEntity(
                        destino.getId(), sku.id(), lote, saldoOrigem.getValidadeLote())));

        BigDecimal fisicoOrigem = saldoOrigem.getSaldoFisico();
        BigDecimal reservadoOrigem = saldoOrigem.getSaldoReservado();
        BigDecimal fisicoDestino = saldoDestino.getSaldoFisico();
        BigDecimal reservadoDestino = saldoDestino.getSaldoReservado();
        saldoOrigem.sair(quantidade);
        saldoDestino.entrar(quantidade);
        saldos.saveAll(List.of(saldoOrigem, saldoDestino));

        registrarMovimento(
                requisicao.empresaId(), requisicao.filialId(), saldoOrigem, MovementType.TRANSFERENCIA_SAIDA,
                quantidade, fisicoOrigem, reservadoOrigem, StockOriginType.TRANSFERENCIA, transferenciaId,
                requisicao.justificativa().trim());
        registrarMovimento(
                requisicao.empresaId(), requisicao.filialId(), saldoDestino, MovementType.TRANSFERENCIA_ENTRADA,
                quantidade, fisicoDestino, reservadoDestino, StockOriginType.TRANSFERENCIA, transferenciaId,
                requisicao.justificativa().trim());
        return List.of(resposta(saldoOrigem), resposta(saldoDestino));
    }

    @Transactional
    public ReservationResult reservar(ReserveStockCommand comando) {
        validarComandoReserva(comando);
        WarehouseEntity deposito = validarContexto(
                comando.empresaId(), comando.filialId(), comando.depositoId(), comando.skuId());
        SkuSnapshot sku = catalogo.obterSku(comando.skuId());
        BigDecimal quantidade = validarQuantidade(comando.quantidade(), sku);
        if (comando.expiraEm() == null || !comando.expiraEm().isAfter(relogio.instant())) {
            throw new BusinessRuleException("EXPIRACAO_INVALIDA", "A reserva deve expirar em um instante futuro.");
        }

        List<StockReservationEntity> existentes = reservas.findByTipoOrigemAndOrigemIdAndStatusIn(
                comando.tipoOrigem(), comando.origemId(), Set.of(ReservationStatus.ATIVA));
        List<StockReservationEntity> existentesDoSku = existentes.stream()
                .filter(item -> item.getSkuId().equals(comando.skuId()))
                .toList();
        if (!existentesDoSku.isEmpty()) {
            BigDecimal total = existentesDoSku.stream()
                    .map(StockReservationEntity::getQuantidade)
                    .reduce(zero(), BigDecimal::add);
            if (total.compareTo(quantidade) == 0) {
                return respostaReserva(comando.origemId(), comando.skuId(), existentesDoSku);
            }
            throw new BusinessRuleException(
                    "RESERVA_DIVERGENTE", "A origem ja possui uma reserva ativa com quantidade diferente.");
        }

        travar(deposito.getId(), sku.id());
        List<StockBalanceEntity> candidatos = saldos.buscarDisponiveisFefo(deposito.getId(), sku.id());
        BigDecimal disponivel = candidatos.stream()
                .map(StockBalanceEntity::disponivel)
                .reduce(zero(), BigDecimal::add);
        if (disponivel.compareTo(quantidade) < 0) {
            throw new BusinessRuleException(
                    "SALDO_INSUFICIENTE",
                    "Saldo disponivel de " + disponivel.toPlainString() + " para uma reserva de "
                            + quantidade.toPlainString() + ".");
        }

        BigDecimal restante = quantidade;
        List<StockReservationEntity> criadas = new ArrayList<>();
        for (StockBalanceEntity saldo : candidatos) {
            if (restante.signum() == 0) {
                break;
            }
            BigDecimal parte = saldo.disponivel().min(restante);
            BigDecimal fisicoAnterior = saldo.getSaldoFisico();
            BigDecimal reservadoAnterior = saldo.getSaldoReservado();
            saldo.reservar(parte);
            StockReservationEntity reserva = reservas.save(new StockReservationEntity(
                    comando.empresaId(), comando.filialId(), comando.depositoId(), comando.skuId(), saldo.getLote(),
                    parte, comando.tipoOrigem(), comando.origemId(), comando.expiraEm()));
            criadas.add(reserva);
            registrarMovimento(
                    comando.empresaId(), comando.filialId(), saldo, MovementType.RESERVA, parte,
                    fisicoAnterior, reservadoAnterior, comando.tipoOrigem(), comando.origemId(),
                    "Reserva automatica por ordem FEFO.");
            restante = restante.subtract(parte);
        }
        saldos.saveAll(candidatos);
        return respostaReserva(comando.origemId(), comando.skuId(), criadas);
    }

    @Transactional
    public void consumirReservas(StockOriginType tipoOrigem, UUID origemId) {
        List<StockReservationEntity> ativas = reservas.findByTipoOrigemAndOrigemIdAndStatusIn(
                tipoOrigem, origemId, Set.of(ReservationStatus.ATIVA));
        if (ativas.isEmpty()) {
            throw new BusinessRuleException("RESERVA_NAO_ENCONTRADA", "Nao existe reserva ativa para a origem.");
        }
        for (StockReservationEntity reserva : ativas) {
            travar(reserva.getDepositoId(), reserva.getSkuId());
            StockBalanceEntity saldo = saldos.findByDepositoIdAndSkuIdAndLote(
                            reserva.getDepositoId(), reserva.getSkuId(), reserva.getLote())
                    .orElseThrow(() -> new BusinessRuleException(
                            "SALDO_INCONSISTENTE", "O saldo vinculado a reserva nao foi encontrado."));
            BigDecimal fisicoAnterior = saldo.getSaldoFisico();
            BigDecimal reservadoAnterior = saldo.getSaldoReservado();
            saldo.consumirReserva(reserva.getQuantidade());
            reserva.consumir();
            registrarMovimento(
                    reserva.getEmpresaId(), reserva.getFilialId(), saldo, MovementType.BAIXA_RESERVA,
                    reserva.getQuantidade(), fisicoAnterior, reservadoAnterior, tipoOrigem, origemId,
                    "Baixa da reserva apos faturamento.");
        }
        saldos.flush();
    }

    @Transactional
    public void liberarReservas(StockOriginType tipoOrigem, UUID origemId) {
        liberar(tipoOrigem, origemId, false);
    }

    @Transactional
    public StockBalanceResponse receber(ReceiveStockCommand comando) {
        WarehouseEntity deposito = validarContexto(
                comando.empresaId(), comando.filialId(), comando.depositoId(), comando.skuId());
        SkuSnapshot sku = catalogo.obterSku(comando.skuId());
        BigDecimal quantidade = validarQuantidade(comando.quantidade(), sku);
        String lote = normalizarLote(comando.lote(), sku);
        validarValidade(comando.validadeLote());
        StockBalanceEntity saldo = obterSaldoBloqueado(
                deposito.getId(), comando.skuId(), lote, comando.validadeLote());
        BigDecimal fisicoAnterior = saldo.getSaldoFisico();
        BigDecimal reservadoAnterior = saldo.getSaldoReservado();
        saldo.entrar(quantidade);
        registrarMovimento(
                comando.empresaId(), comando.filialId(), saldo, MovementType.RECEBIMENTO_COMPRA, quantidade,
                fisicoAnterior, reservadoAnterior, comando.tipoOrigem(), comando.origemId(),
                "Recebimento de mercadoria.");
        return resposta(saldo);
    }

    @Transactional(readOnly = true)
    public List<StockBalanceResponse> consultarSaldos(UUID depositoId, UUID skuId) {
        WarehouseEntity deposito = obterDeposito(depositoId);
        acessoFilial.garantirAcesso(deposito.getFilialId());
        return saldos.findByDepositoIdAndSkuIdOrderByValidadeLoteAsc(depositoId, skuId).stream()
                .map(InventoryService::resposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse validarDeposito(UUID empresaId, UUID filialId, UUID depositoId) {
        var filial = acessoFilial.garantirAcesso(filialId);
        WarehouseEntity deposito = obterDeposito(depositoId);
        if (!filial.empresaId().equals(empresaId)
                || !deposito.getEmpresaId().equals(empresaId)
                || !deposito.getFilialId().equals(filialId)) {
            throw new BusinessRuleException(
                    "DEPOSITO_FORA_DO_CONTEXTO", "O deposito nao pertence a empresa e filial informadas.");
        }
        if (!deposito.isAtivo()) {
            throw new BusinessRuleException("DEPOSITO_INATIVO", "O deposito esta inativo.");
        }
        return resposta(deposito);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> consultarMovimentacoes(
            UUID depositoId, UUID skuId, int pagina, int tamanho) {
        WarehouseEntity deposito = obterDeposito(depositoId);
        acessoFilial.garantirAcesso(deposito.getFilialId());
        var paginacao = PageRequest.of(
                pagina, Math.min(tamanho, 100), Sort.by(Sort.Direction.DESC, "ocorridoEm"));
        return PageResponse.de(movimentacoes.findByDepositoIdAndSkuId(depositoId, skuId, paginacao)
                .map(InventoryService::resposta));
    }

    @Scheduled(fixedDelayString = "${commercecore.estoque.intervalo-liberacao-reservas:60000}")
    @Transactional
    public void liberarReservasExpiradas() {
        List<StockReservationEntity> expiradas = reservas
                .findTop100ByStatusAndExpiraEmBeforeOrderByExpiraEmAsc(ReservationStatus.ATIVA, relogio.instant());
        expiradas.stream()
                .map(reserva -> new OrigemReserva(reserva.getTipoOrigem(), reserva.getOrigemId()))
                .distinct()
                .forEach(origem -> liberar(origem.tipo(), origem.id(), true));
    }

    private void liberar(StockOriginType tipoOrigem, UUID origemId, boolean expirada) {
        List<StockReservationEntity> ativas = reservas.findByTipoOrigemAndOrigemIdAndStatusIn(
                tipoOrigem, origemId, Set.of(ReservationStatus.ATIVA));
        for (StockReservationEntity reserva : ativas) {
            travar(reserva.getDepositoId(), reserva.getSkuId());
            StockBalanceEntity saldo = saldos.findByDepositoIdAndSkuIdAndLote(
                            reserva.getDepositoId(), reserva.getSkuId(), reserva.getLote())
                    .orElseThrow(() -> new BusinessRuleException(
                            "SALDO_INCONSISTENTE", "O saldo vinculado a reserva nao foi encontrado."));
            BigDecimal fisicoAnterior = saldo.getSaldoFisico();
            BigDecimal reservadoAnterior = saldo.getSaldoReservado();
            saldo.liberarReserva(reserva.getQuantidade());
            reserva.liberar(expirada);
            registrarMovimento(
                    reserva.getEmpresaId(), reserva.getFilialId(), saldo, MovementType.LIBERACAO_RESERVA,
                    reserva.getQuantidade(), fisicoAnterior, reservadoAnterior, tipoOrigem, origemId,
                    expirada ? "Reserva expirada automaticamente." : "Reserva liberada pelo cancelamento da origem.");
        }
    }

    private WarehouseEntity validarContexto(UUID empresaId, UUID filialId, UUID depositoId, UUID skuId) {
        var filial = acessoFilial.garantirAcesso(filialId);
        WarehouseEntity deposito = obterDeposito(depositoId);
        SkuSnapshot sku = catalogo.obterSku(skuId);
        if (!filial.empresaId().equals(empresaId)
                || !deposito.getEmpresaId().equals(empresaId)
                || !deposito.getFilialId().equals(filialId)
                || !sku.empresaId().equals(empresaId)) {
            throw new BusinessRuleException(
                    "CONTEXTO_EMPRESARIAL_INVALIDO", "Empresa, filial, deposito e SKU devem pertencer ao mesmo contexto.");
        }
        if (!deposito.isAtivo()) {
            throw new BusinessRuleException("DEPOSITO_INATIVO", "O deposito esta inativo.");
        }
        return deposito;
    }

    private WarehouseEntity obterDeposito(UUID depositoId) {
        return depositos.findById(depositoId)
                .orElseThrow(() -> new NotFoundException("Deposito nao encontrado."));
    }

    private StockBalanceEntity obterSaldoBloqueado(
            UUID depositoId, UUID skuId, String lote, LocalDate validadeLote) {
        travar(depositoId, skuId);
        return saldos.findByDepositoIdAndSkuIdAndLote(depositoId, skuId, lote)
                .map(existente -> {
                    if (validadeLote != null
                            && existente.getValidadeLote() != null
                            && !existente.getValidadeLote().equals(validadeLote)) {
                        throw new BusinessRuleException(
                                "VALIDADE_DIVERGENTE", "O lote ja existe com outra data de validade.");
                    }
                    return existente;
                })
                .orElseGet(() -> saldos.save(new StockBalanceEntity(depositoId, skuId, lote, validadeLote)));
    }

    private void registrarMovimento(
            UUID empresaId,
            UUID filialId,
            StockBalanceEntity saldo,
            MovementType tipo,
            BigDecimal quantidade,
            BigDecimal fisicoAnterior,
            BigDecimal reservadoAnterior,
            StockOriginType tipoOrigem,
            UUID origemId,
            String justificativa) {
        movimentacoes.save(new StockMovementEntity(
                empresaId, filialId, saldo.getDepositoId(), saldo.getSkuId(), saldo.getLote(), tipo, quantidade,
                fisicoAnterior, saldo.getSaldoFisico(), reservadoAnterior, saldo.getSaldoReservado(), tipoOrigem,
                origemId, justificativa, atorAtual.id(), relogio.instant()));
    }

    private void travar(UUID depositoId, UUID skuId) {
        bloqueios.travar(depositoId + ":" + skuId);
    }

    private BigDecimal validarQuantidade(BigDecimal quantidade, SkuSnapshot sku) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new BusinessRuleException("QUANTIDADE_INVALIDA", "A quantidade deve ser maior que zero.");
        }
        BigDecimal normalizada;
        try {
            normalizada = quantidade.setScale(3, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException excecao) {
            throw new BusinessRuleException("ESCALA_INVALIDA", "A quantidade aceita no maximo tres casas decimais.");
        }
        if (!sku.aceitaFracionado() && normalizada.stripTrailingZeros().scale() > 0) {
            throw new BusinessRuleException("SKU_NAO_FRACIONADO", "Este SKU aceita apenas quantidades inteiras.");
        }
        return normalizada;
    }

    private String normalizarLote(String lote, SkuSnapshot sku) {
        if (sku.controlaLote() && (lote == null || lote.isBlank())) {
            throw new BusinessRuleException("LOTE_OBRIGATORIO", "O lote e obrigatorio para este SKU.");
        }
        return lote == null || lote.isBlank() ? SEM_LOTE : lote.trim().toUpperCase();
    }

    private void validarValidade(LocalDate validade) {
        if (validade != null && validade.isBefore(LocalDate.now(relogio))) {
            throw new BusinessRuleException("LOTE_VENCIDO", "Nao e permitido receber saldo de um lote vencido.");
        }
    }

    private void validarComandoReserva(ReserveStockCommand comando) {
        if (comando == null || comando.empresaId() == null || comando.filialId() == null
                || comando.depositoId() == null || comando.skuId() == null || comando.tipoOrigem() == null
                || comando.origemId() == null) {
            throw new BusinessRuleException("RESERVA_INVALIDA", "Todos os dados de contexto da reserva sao obrigatorios.");
        }
    }

    private static WarehouseResponse resposta(WarehouseEntity deposito) {
        return new WarehouseResponse(
                deposito.getId(), deposito.getEmpresaId(), deposito.getFilialId(), deposito.getCodigo(),
                deposito.getNome(), deposito.isAtivo());
    }

    private static StockBalanceResponse resposta(StockBalanceEntity saldo) {
        return new StockBalanceResponse(
                saldo.getDepositoId(), saldo.getSkuId(), exibirLote(saldo.getLote()), saldo.getValidadeLote(),
                saldo.getSaldoFisico(), saldo.getSaldoReservado(), saldo.disponivel());
    }

    private static StockMovementResponse resposta(StockMovementEntity movimento) {
        return new StockMovementResponse(
                movimento.getId(), movimento.getDepositoId(), movimento.getSkuId(), exibirLote(movimento.getLote()),
                movimento.getTipo(), movimento.getQuantidade(), movimento.getSaldoFisicoAnterior(),
                movimento.getSaldoFisicoPosterior(), movimento.getSaldoReservadoAnterior(),
                movimento.getSaldoReservadoPosterior(), movimento.getTipoOrigem(), movimento.getOrigemId(),
                movimento.getJustificativa(), movimento.getRealizadoPor(), movimento.getOcorridoEm());
    }

    private static ReservationResult respostaReserva(
            UUID origemId, UUID skuId, List<StockReservationEntity> reservas) {
        BigDecimal total = reservas.stream()
                .filter(item -> item.getSkuId().equals(skuId))
                .map(StockReservationEntity::getQuantidade)
                .reduce(zero(), BigDecimal::add);
        Instant expiraEm = reservas.stream().map(StockReservationEntity::getExpiraEm).min(Instant::compareTo).orElse(null);
        return new ReservationResult(
                origemId, skuId, total, expiraEm,
                reservas.stream().filter(item -> item.getSkuId().equals(skuId))
                        .map(item -> new ReservationResult.ReservationPart(
                                item.getId(), exibirLote(item.getLote()), item.getQuantidade()))
                        .toList());
    }

    private static String exibirLote(String lote) {
        return SEM_LOTE.equals(lote) ? null : lote;
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(3);
    }

    private record OrigemReserva(StockOriginType tipo, UUID id) {
    }
}
