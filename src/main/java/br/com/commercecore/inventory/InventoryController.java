package br.com.commercecore.inventory;

import br.com.commercecore.shared.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/estoque")
@Tag(name = "Estoque")
public class InventoryController {

    private final InventoryService estoque;

    public InventoryController(InventoryService estoque) {
        this.estoque = estoque;
    }

    @PostMapping("/depositos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE')")
    @Operation(summary = "Cria um deposito vinculado a uma filial")
    ResponseEntity<WarehouseResponse> criarDeposito(@Valid @RequestBody CreateWarehouseRequest requisicao) {
        WarehouseResponse resposta = estoque.criarDeposito(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/estoque/depositos/" + resposta.id())).body(resposta);
    }

    @PostMapping("/ajustes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE', 'ESTOQUISTA')")
    @Operation(summary = "Registra uma entrada ou saida manual justificada")
    StockBalanceResponse ajustar(@Valid @RequestBody AdjustStockRequest requisicao) {
        return estoque.ajustar(requisicao);
    }

    @PostMapping("/transferencias")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE', 'ESTOQUISTA')")
    @Operation(summary = "Transfere saldo entre depositos na mesma filial")
    List<StockBalanceResponse> transferir(@Valid @RequestBody TransferStockRequest requisicao) {
        return estoque.transferir(requisicao);
    }

    @GetMapping("/depositos/{depositoId}/skus/{skuId}/saldos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE', 'ESTOQUISTA', 'VENDEDOR', 'COMPRADOR')")
    @Operation(summary = "Consulta saldo fisico, reservado e disponivel por lote")
    List<StockBalanceResponse> consultarSaldos(
            @PathVariable UUID depositoId, @PathVariable UUID skuId) {
        return estoque.consultarSaldos(depositoId, skuId);
    }

    @GetMapping("/depositos/{depositoId}/skus/{skuId}/movimentacoes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GERENTE_ESTOQUE', 'AUDITOR')")
    @Operation(summary = "Consulta o razao imutavel de movimentacoes de um SKU")
    PageResponse<StockMovementResponse> consultarMovimentacoes(
            @PathVariable UUID depositoId,
            @PathVariable UUID skuId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return estoque.consultarMovimentacoes(depositoId, skuId, pagina, tamanho);
    }
}
