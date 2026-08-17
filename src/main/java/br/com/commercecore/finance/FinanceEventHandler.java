package br.com.commercecore.finance;

import br.com.commercecore.sales.SaleInvoicedEvent;
import br.com.commercecore.purchasing.PurchaseReceivedEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class FinanceEventHandler {

    private final FinanceService financeiro;

    public FinanceEventHandler(FinanceService financeiro) {
        this.financeiro = financeiro;
    }

    @ApplicationModuleListener
    public void aoFaturarVenda(SaleInvoicedEvent evento) {
        financeiro.criarRecebiveis(evento);
    }

    @ApplicationModuleListener
    public void aoReceberCompra(PurchaseReceivedEvent evento) {
        financeiro.criarContasPagar(new PayableCreatedCommand(
                evento.eventoId(), evento.recebimentoId(), evento.numeroRecebimento(), evento.empresaId(),
                evento.filialId(), evento.fornecedorId(), evento.fornecedorNome(), evento.valorTotal(),
                evento.numeroParcelas(), evento.primeiroVencimento(), evento.recebidoEm()));
    }
}
