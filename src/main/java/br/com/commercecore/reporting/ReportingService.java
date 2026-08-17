package br.com.commercecore.reporting;

import br.com.commercecore.organization.BranchAccessService;
import br.com.commercecore.shared.BusinessRuleException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportingService {

    private final JdbcClient banco;
    private final BranchAccessService acessoFilial;

    public ReportingService(JdbcClient banco, BranchAccessService acessoFilial) {
        this.banco = banco;
        this.acessoFilial = acessoFilial;
    }

    @Transactional(readOnly = true)
    public ManagementSummaryResponse resumo(
            UUID empresaId, UUID filialId, LocalDate inicio, LocalDate fim) {
        validarContexto(empresaId, filialId, inicio, fim);
        var inicioDataHora = inicio.atStartOfDay().atOffset(ZoneOffset.UTC);
        var fimExclusivo = fim.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        ResumoVendas vendas = banco.sql("""
                select count(*) as quantidade, coalesce(sum(valor_total), 0) as total
                from fatura_venda
                where empresa_id = :empresaId and filial_id = :filialId
                  and emitida_em >= :inicio and emitida_em < :fim
                """)
                .param("empresaId", empresaId).param("filialId", filialId)
                .param("inicio", inicioDataHora).param("fim", fimExclusivo)
                .query((resultado, linha) -> new ResumoVendas(
                        resultado.getLong("quantidade"), resultado.getBigDecimal("total")))
                .single();

        BigDecimal compras = banco.sql("""
                select coalesce(sum(r.valor_total), 0)
                from recebimento_compra r
                join pedido_compra p on p.id = r.pedido_id
                where p.empresa_id = :empresaId and p.filial_id = :filialId
                  and r.recebido_em >= :inicio and r.recebido_em < :fim
                """)
                .param("empresaId", empresaId).param("filialId", filialId)
                .param("inicio", inicioDataHora).param("fim", fimExclusivo)
                .query(BigDecimal.class).single();

        BigDecimal receber = totalAberto(empresaId, filialId, "RECEBER");
        BigDecimal pagar = totalAberto(empresaId, filialId, "PAGAR");
        long reposicao = itensReposicao(empresaId, filialId).size();
        return new ManagementSummaryResponse(
                empresaId, filialId, inicio, fim, vendas.quantidade(), vendas.total(), compras,
                receber, pagar, reposicao);
    }

    @Transactional(readOnly = true)
    public List<ReplenishmentItemResponse> itensReposicao(UUID empresaId, UUID filialId) {
        validarFilial(empresaId, filialId);
        return banco.sql("""
                select s.id as sku_id, s.codigo as codigo_sku, p.nome as nome_produto,
                       s.estoque_minimo,
                       coalesce(sum(se.saldo_fisico), 0) as saldo_fisico,
                       coalesce(sum(se.saldo_reservado), 0) as saldo_reservado,
                       coalesce(sum(se.saldo_fisico - se.saldo_reservado), 0) as saldo_disponivel
                from sku s
                join produto p on p.id = s.produto_id
                left join deposito d on d.filial_id = :filialId and d.ativo = true
                left join saldo_estoque se on se.deposito_id = d.id and se.sku_id = s.id
                where p.empresa_id = :empresaId and p.ativo = true and s.ativo = true
                group by s.id, s.codigo, p.nome, s.estoque_minimo
                having coalesce(sum(se.saldo_fisico - se.saldo_reservado), 0) <= s.estoque_minimo
                order by (s.estoque_minimo - coalesce(sum(se.saldo_fisico - se.saldo_reservado), 0)) desc,
                         p.nome, s.codigo
                """)
                .param("empresaId", empresaId).param("filialId", filialId)
                .query((resultado, linha) -> {
                    BigDecimal minimo = resultado.getBigDecimal("estoque_minimo");
                    BigDecimal disponivel = resultado.getBigDecimal("saldo_disponivel");
                    return new ReplenishmentItemResponse(
                            resultado.getObject("sku_id", UUID.class), resultado.getString("codigo_sku"),
                            resultado.getString("nome_produto"), minimo,
                            resultado.getBigDecimal("saldo_fisico"), resultado.getBigDecimal("saldo_reservado"),
                            disponivel, minimo.subtract(disponivel).max(BigDecimal.ZERO));
                }).list();
    }

    @Transactional(readOnly = true)
    public AgingResponse inadimplencia(UUID empresaId, UUID filialId, LocalDate referencia) {
        validarFilial(empresaId, filialId);
        return banco.sql("""
                select
                    coalesce(sum(saldo) filter (where data_vencimento >= :referencia), 0) as a_vencer,
                    coalesce(sum(saldo) filter (where data_vencimento < :referencia
                        and data_vencimento >= :referenciaMinus30), 0) as vencido_ate_30,
                    coalesce(sum(saldo) filter (where data_vencimento < :referenciaMinus30
                        and data_vencimento >= :referenciaMinus60), 0) as vencido_31_60,
                    coalesce(sum(saldo) filter (where data_vencimento < :referenciaMinus60
                        and data_vencimento >= :referenciaMinus90), 0) as vencido_61_90,
                    coalesce(sum(saldo) filter (where data_vencimento < :referenciaMinus90), 0) as vencido_acima_90,
                    coalesce(sum(saldo), 0) as total
                from titulo_financeiro
                where empresa_id = :empresaId and filial_id = :filialId
                  and tipo = 'RECEBER' and status in ('ABERTO', 'PARCIAL')
                """)
                .param("empresaId", empresaId).param("filialId", filialId)
                .param("referencia", referencia)
                .param("referenciaMinus30", referencia.minusDays(30))
                .param("referenciaMinus60", referencia.minusDays(60))
                .param("referenciaMinus90", referencia.minusDays(90))
                .query((resultado, linha) -> new AgingResponse(
                        empresaId, filialId, referencia, resultado.getBigDecimal("a_vencer"),
                        resultado.getBigDecimal("vencido_ate_30"), resultado.getBigDecimal("vencido_31_60"),
                        resultado.getBigDecimal("vencido_61_90"), resultado.getBigDecimal("vencido_acima_90"),
                        resultado.getBigDecimal("total")))
                .single();
    }

    @Transactional(readOnly = true)
    public byte[] exportar(UUID empresaId, UUID filialId, LocalDate inicio, LocalDate fim) {
        ManagementSummaryResponse resumo = resumo(empresaId, filialId, inicio, fim);
        List<ReplenishmentItemResponse> reposicao = itensReposicao(empresaId, filialId);
        AgingResponse aging = inadimplencia(empresaId, filialId, fim);
        try (Workbook pasta = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            CellStyle cabecalho = estiloCabecalho(pasta);
            preencherResumo(pasta.createSheet("Resumo gerencial"), cabecalho, resumo);
            preencherReposicao(pasta.createSheet("Reposicao de estoque"), cabecalho, reposicao);
            preencherAging(pasta.createSheet("Inadimplencia"), cabecalho, aging);
            pasta.write(saida);
            return saida.toByteArray();
        } catch (IOException excecao) {
            throw new IllegalStateException("Nao foi possivel gerar a planilha gerencial.", excecao);
        }
    }

    private BigDecimal totalAberto(UUID empresaId, UUID filialId, String tipo) {
        return banco.sql("""
                select coalesce(sum(saldo), 0) from titulo_financeiro
                where empresa_id = :empresaId and filial_id = :filialId
                  and tipo = :tipo and status in ('ABERTO', 'PARCIAL')
                """)
                .param("empresaId", empresaId).param("filialId", filialId).param("tipo", tipo)
                .query(BigDecimal.class).single();
    }

    private void validarContexto(UUID empresaId, UUID filialId, LocalDate inicio, LocalDate fim) {
        validarFilial(empresaId, filialId);
        if (fim.isBefore(inicio) || inicio.plusYears(2).isBefore(fim)) {
            throw new BusinessRuleException(
                    "PERIODO_RELATORIO_INVALIDO", "O fim deve ser igual ou posterior ao inicio e o periodo aceita ate dois anos.");
        }
    }

    private void validarFilial(UUID empresaId, UUID filialId) {
        var filial = acessoFilial.garantirAcesso(filialId);
        if (!filial.empresaId().equals(empresaId)) {
            throw new BusinessRuleException("FILIAL_DE_OUTRA_EMPRESA", "A filial nao pertence a empresa informada.");
        }
    }

    private static CellStyle estiloCabecalho(Workbook pasta) {
        CellStyle estilo = pasta.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        var fonte = pasta.createFont();
        fonte.setColor(IndexedColors.WHITE.getIndex());
        fonte.setBold(true);
        estilo.setFont(fonte);
        return estilo;
    }

    private static void preencherResumo(Sheet planilha, CellStyle estilo, ManagementSummaryResponse resumo) {
        String[] cabecalhos = {"Indicador", "Valor"};
        cabecalho(planilha, estilo, cabecalhos);
        Object[][] linhas = {
                {"Periodo", resumo.inicio() + " a " + resumo.fim()},
                {"Vendas faturadas", resumo.vendasFaturadas()},
                {"Faturamento", resumo.faturamento()},
                {"Compras recebidas", resumo.comprasRecebidas()},
                {"Contas a receber em aberto", resumo.contasReceberEmAberto()},
                {"Contas a pagar em aberto", resumo.contasPagarEmAberto()},
                {"SKUs para reposicao", resumo.skusParaReposicao()}
        };
        for (int indice = 0; indice < linhas.length; indice++) {
            Row linha = planilha.createRow(indice + 1);
            texto(linha, 0, String.valueOf(linhas[indice][0]));
            valor(linha, 1, linhas[indice][1]);
        }
        ajustar(planilha, cabecalhos.length);
    }

    private static void preencherReposicao(
            Sheet planilha, CellStyle estilo, List<ReplenishmentItemResponse> itens) {
        String[] cabecalhos = {
                "SKU", "Produto", "Estoque minimo", "Fisico", "Reservado", "Disponivel", "Compra sugerida"
        };
        cabecalho(planilha, estilo, cabecalhos);
        for (int indice = 0; indice < itens.size(); indice++) {
            ReplenishmentItemResponse item = itens.get(indice);
            Row linha = planilha.createRow(indice + 1);
            texto(linha, 0, item.codigoSku());
            texto(linha, 1, item.nomeProduto());
            numero(linha, 2, item.estoqueMinimo());
            numero(linha, 3, item.saldoFisico());
            numero(linha, 4, item.saldoReservado());
            numero(linha, 5, item.saldoDisponivel());
            numero(linha, 6, item.quantidadeSugerida());
        }
        ajustar(planilha, cabecalhos.length);
    }

    private static void preencherAging(Sheet planilha, CellStyle estilo, AgingResponse aging) {
        String[] cabecalhos = {"Faixa", "Saldo"};
        cabecalho(planilha, estilo, cabecalhos);
        Object[][] linhas = {
                {"A vencer", aging.aVencer()}, {"Vencido ate 30 dias", aging.vencidoAte30Dias()},
                {"Vencido de 31 a 60 dias", aging.vencido31A60Dias()},
                {"Vencido de 61 a 90 dias", aging.vencido61A90Dias()},
                {"Vencido acima de 90 dias", aging.vencidoAcima90Dias()}, {"Total", aging.totalEmAberto()}
        };
        for (int indice = 0; indice < linhas.length; indice++) {
            Row linha = planilha.createRow(indice + 1);
            texto(linha, 0, String.valueOf(linhas[indice][0]));
            valor(linha, 1, linhas[indice][1]);
        }
        ajustar(planilha, cabecalhos.length);
    }

    private static void cabecalho(Sheet planilha, CellStyle estilo, String[] titulos) {
        Row linha = planilha.createRow(0);
        for (int coluna = 0; coluna < titulos.length; coluna++) {
            var celula = linha.createCell(coluna);
            celula.setCellValue(titulos[coluna]);
            celula.setCellStyle(estilo);
        }
        planilha.createFreezePane(0, 1);
    }

    private static void valor(Row linha, int coluna, Object valor) {
        if (valor instanceof Number numero) {
            linha.createCell(coluna).setCellValue(numero.doubleValue());
        } else {
            texto(linha, coluna, String.valueOf(valor));
        }
    }

    private static void numero(Row linha, int coluna, BigDecimal valor) {
        linha.createCell(coluna).setCellValue(valor.doubleValue());
    }

    private static void texto(Row linha, int coluna, String valor) {
        linha.createCell(coluna).setCellValue(valor);
    }

    private static void ajustar(Sheet planilha, int colunas) {
        for (int coluna = 0; coluna < colunas; coluna++) {
            planilha.autoSizeColumn(coluna);
            planilha.setColumnWidth(coluna, Math.min(planilha.getColumnWidth(coluna) + 700, 18000));
        }
    }

    private record ResumoVendas(long quantidade, BigDecimal total) {
    }
}
