package br.com.commercecore.catalog;

import br.com.commercecore.catalog.internal.CategoryEntity;
import br.com.commercecore.catalog.internal.CategoryRepository;
import br.com.commercecore.catalog.internal.ProductEntity;
import br.com.commercecore.catalog.internal.ProductRepository;
import br.com.commercecore.catalog.internal.SkuEntity;
import br.com.commercecore.catalog.internal.SkuRepository;
import br.com.commercecore.shared.BusinessRuleException;
import br.com.commercecore.shared.NotFoundException;
import br.com.commercecore.shared.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final CategoryRepository categorias;
    private final ProductRepository produtos;
    private final SkuRepository skus;

    public CatalogService(CategoryRepository categorias, ProductRepository produtos, SkuRepository skus) {
        this.categorias = categorias;
        this.produtos = produtos;
        this.skus = skus;
    }

    @Transactional
    public CategoryResponse criarCategoria(CreateCategoryRequest requisicao) {
        String nome = requisicao.nome().trim();
        if (categorias.existsByEmpresaIdAndNomeIgnoreCase(requisicao.empresaId(), nome)) {
            throw new BusinessRuleException("CATEGORIA_DUPLICADA", "Ja existe uma categoria com este nome.");
        }
        CategoryEntity categoria = categorias.save(new CategoryEntity(requisicao.empresaId(), nome));
        return new CategoryResponse(categoria.getId(), categoria.getEmpresaId(), categoria.getNome(), categoria.isAtiva());
    }

    @Transactional
    public ProductResponse criarProduto(CreateProductRequest requisicao) {
        CategoryEntity categoria = categorias.findById(requisicao.categoriaId())
                .orElseThrow(() -> new NotFoundException("Categoria nao encontrada."));
        if (!categoria.getEmpresaId().equals(requisicao.empresaId()) || !categoria.isAtiva()) {
            throw new BusinessRuleException("CATEGORIA_INVALIDA", "A categoria nao pertence a empresa ou esta inativa.");
        }
        String codigo = requisicao.codigo().trim().toUpperCase();
        if (produtos.existsByEmpresaIdAndCodigo(requisicao.empresaId(), codigo)) {
            throw new BusinessRuleException("PRODUTO_DUPLICADO", "Ja existe um produto com este codigo.");
        }
        ProductEntity produto = new ProductEntity(
                requisicao.empresaId(), requisicao.categoriaId(), codigo, requisicao.nome().trim(), requisicao.descricao());
        for (CreateSkuRequest item : requisicao.skus()) {
            String codigoSku = item.codigo().trim().toUpperCase();
            if (skus.existsByCodigo(codigoSku)) {
                throw new BusinessRuleException("SKU_DUPLICADO", "Ja existe um SKU com o codigo " + codigoSku + ".");
            }
            produto.adicionarSku(new SkuEntity(
                    codigoSku,
                    normalizarOpcional(item.codigoBarras()),
                    normalizarOpcional(item.descricaoVariacao()),
                    item.unidadeMedida(),
                    item.controlaLote(),
                    item.aceitaFracionado(),
                    item.estoqueMinimo()));
        }
        return resposta(produtos.save(produto));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listar(UUID empresaId, int pagina, int tamanho) {
        var paginacao = PageRequest.of(pagina, Math.min(tamanho, 100), Sort.by("nome").ascending());
        return PageResponse.de(produtos.findByEmpresaId(empresaId, paginacao).map(CatalogService::resposta));
    }

    @Transactional(readOnly = true)
    public SkuSnapshot obterSku(UUID skuId) {
        SkuEntity sku = skus.findById(skuId).orElseThrow(() -> new NotFoundException("SKU nao encontrado."));
        ProductEntity produto = sku.getProduto();
        if (!sku.isAtivo() || !produto.isAtivo()) {
            throw new BusinessRuleException("SKU_INATIVO", "O SKU ou produto esta inativo.");
        }
        return new SkuSnapshot(
                sku.getId(), produto.getEmpresaId(), sku.getCodigo(), produto.getNome(), sku.getUnidadeMedida(),
                sku.isControlaLote(), sku.isAceitaFracionado(), sku.getEstoqueMinimo(), sku.isAtivo());
    }

    private static ProductResponse resposta(ProductEntity produto) {
        return new ProductResponse(
                produto.getId(), produto.getEmpresaId(), produto.getCategoriaId(), produto.getCodigo(), produto.getNome(),
                produto.getDescricao(), produto.isAtivo(), produto.getVersao(),
                produto.getSkus().stream().map(CatalogService::resposta).toList());
    }

    private static SkuResponse resposta(SkuEntity sku) {
        return new SkuResponse(
                sku.getId(), sku.getCodigo(), sku.getCodigoBarras(), sku.getDescricaoVariacao(), sku.getUnidadeMedida(),
                sku.isControlaLote(), sku.isAceitaFracionado(), sku.getEstoqueMinimo(), sku.isAtivo());
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
