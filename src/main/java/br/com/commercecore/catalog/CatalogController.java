package br.com.commercecore.catalog;

import br.com.commercecore.shared.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalogo")
public class CatalogController {

    private final CatalogService servico;

    public CatalogController(CatalogService servico) {
        this.servico = servico;
    }

    @PostMapping("/categorias")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE_COMERCIAL','GERENTE_ESTOQUE')")
    ResponseEntity<CategoryResponse> criarCategoria(@Valid @RequestBody CreateCategoryRequest requisicao) {
        CategoryResponse resposta = servico.criarCategoria(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/catalogo/categorias/" + resposta.id())).body(resposta);
    }

    @PostMapping("/produtos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','GERENTE_ESTOQUE','ESTOQUISTA')")
    ResponseEntity<ProductResponse> criarProduto(@Valid @RequestBody CreateProductRequest requisicao) {
        ProductResponse resposta = servico.criarProduto(requisicao);
        return ResponseEntity.created(URI.create("/api/v1/catalogo/produtos/" + resposta.id())).body(resposta);
    }

    @GetMapping("/produtos")
    PageResponse<ProductResponse> listarProdutos(
            @RequestParam UUID empresaId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return servico.listar(empresaId, pagina, tamanho);
    }
}
