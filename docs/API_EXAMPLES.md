# Exemplos da API

Os exemplos usam os dados carregados pelo perfil `demo`. Datas futuras devem ser ajustadas quando necessário.

## Contexto local

```text
API: http://localhost:8082
Keycloak: http://localhost:18083
empresaId: 11111111-1111-1111-1111-111111111111
filialId: 22222222-2222-2222-2222-222222222222
clienteId: 33333333-3333-3333-3333-333333333333
fornecedorId: 44444444-4444-4444-4444-444444444444
categoriaId: 55555555-5555-5555-5555-555555555555
produtoId: 66666666-6666-6666-6666-666666666666
skuId: 77777777-7777-7777-7777-777777777777
depositoId: 88888888-8888-8888-8888-888888888888
tabelaPrecoId: 99999999-9999-9999-9999-999999999999
contaFinanceiraId: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
```

## Autenticação

Solicite o token em `POST /realms/commercecore/protocol/openid-connect/token`, com `Content-Type: application/x-www-form-urlencoded`:

```text
grant_type=password
client_id=commercecore-postman
username=admin@commercecore.local
password=CommerceCore@123
```

Envie estes cabeçalhos nas rotas de negócio:

```http
Authorization: Bearer <access_token>
X-Empresa-ID: 11111111-1111-1111-1111-111111111111
X-Filial-ID: 22222222-2222-2222-2222-222222222222
X-Correlation-ID: teste-manual-001
```

## Organização

`POST /api/v1/empresas`

```json
{
  "razaoSocial": "Nova Comercio de Tecnologia Ltda",
  "nomeFantasia": "Nova Tech",
  "cnpj": "27865757000102"
}
```

`POST /api/v1/empresas/{empresaId}/filiais`

```json
{
  "codigo": "SP02",
  "nome": "Filial Campinas",
  "cnpj": "11444777000161",
  "fusoHorario": "America/Sao_Paulo"
}
```

## Parceiros

`POST /api/v1/parceiros`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "tipoPessoa": "JURIDICA",
  "nomeRazaoSocial": "Horizonte Distribuidora Ltda",
  "nomeFantasia": "Horizonte",
  "cpfCnpj": "11222333000181",
  "email": "compras@horizonte.local",
  "telefone": "+55 11 3333-4444",
  "papeis": ["CLIENTE", "FORNECEDOR"]
}
```

## Catálogo

`POST /api/v1/catalogo/categorias`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "nome": "Periféricos"
}
```

`POST /api/v1/catalogo/produtos`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "categoriaId": "55555555-5555-5555-5555-555555555555",
  "codigo": "MONITOR-27",
  "nome": "Monitor profissional 27 polegadas",
  "descricao": "Monitor IPS para estações de trabalho",
  "skus": [
    {
      "codigo": "MONITOR-27-QHD",
      "codigoBarras": "7891234567895",
      "descricaoVariacao": "QHD preto",
      "unidadeMedida": "UNIDADE",
      "controlaLote": false,
      "aceitaFracionado": false,
      "estoqueMinimo": 5
    }
  ]
}
```

## Precificação

`POST /api/v1/precificacao/tabelas`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "nome": "Varejo segundo semestre",
  "vigenteDe": "2026-08-01T00:00:00Z",
  "vigenteAte": "2027-01-01T00:00:00Z",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "valorVenda": 5200.00,
      "custoReferencia": 4000.00
    }
  ]
}
```

`POST /api/v1/precificacao/promocoes`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "skuId": "77777777-7777-7777-7777-777777777777",
  "nome": "Cupom de boas-vindas",
  "codigoCupom": "BEMVINDO10",
  "tipoDesconto": "PERCENTUAL",
  "valorDesconto": 10,
  "quantidadeMinima": 1,
  "inicio": "2026-08-01T00:00:00Z",
  "fim": "2027-01-01T00:00:00Z",
  "prioridade": 100
}
```

`POST /api/v1/precificacao/simulacoes`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "skuId": "77777777-7777-7777-7777-777777777777",
  "quantidade": 2,
  "codigoCupom": "BEMVINDO10"
}
```

## Estoque

`POST /api/v1/estoque/depositos`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "codigo": "SECUNDARIO",
  "nome": "Depósito secundário"
}
```

`POST /api/v1/estoque/ajustes`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "depositoId": "88888888-8888-8888-8888-888888888888",
  "skuId": "77777777-7777-7777-7777-777777777777",
  "direcao": "ENTRADA",
  "quantidade": 3,
  "lote": "LOTE-2026-09",
  "validadeLote": "2027-09-30",
  "justificativa": "Contagem e conciliação do inventário"
}
```

`POST /api/v1/estoque/transferencias`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "depositoOrigemId": "88888888-8888-8888-8888-888888888888",
  "depositoDestinoId": "<id-do-deposito-secundario>",
  "skuId": "77777777-7777-7777-7777-777777777777",
  "quantidade": 1,
  "lote": "LOTE-2026-09",
  "justificativa": "Reposição do depósito secundário"
}
```

## Vendas

### 1. Criar orçamento

`POST /api/v1/vendas/orcamentos`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "clienteId": "33333333-3333-3333-3333-333333333333",
  "validoAte": "2026-12-31T23:59:59Z",
  "codigoCupom": "BEMVINDO10",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "quantidade": 2
    }
  ]
}
```

Guarde o `id` retornado como `orcamentoId`.

### 2. Avançar o orçamento

```text
POST /api/v1/vendas/orcamentos/{orcamentoId}/envio
POST /api/v1/vendas/orcamentos/{orcamentoId}/aceite
```

Essas operações não possuem corpo.

### 3. Converter em pedido

`POST /api/v1/vendas/orcamentos/{orcamentoId}/conversao`

```json
{
  "depositoId": "88888888-8888-8888-8888-888888888888",
  "numeroParcelas": 2,
  "primeiroVencimento": "2026-09-15"
}
```

Guarde o `id` como `pedidoVendaId` e execute:

```text
POST /api/v1/vendas/pedidos/{pedidoVendaId}/confirmacao
POST /api/v1/vendas/pedidos/{pedidoVendaId}/faturamento
```

A confirmação reserva por FEFO. O faturamento baixa o estoque e publica o evento que cria contas a receber.

## Compras

### 1. Requisição

`POST /api/v1/compras/requisicoes`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "justificativa": "Reposição planejada do estoque de notebooks",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "quantidade": 10
    }
  ]
}
```

`POST /api/v1/compras/requisicoes/{requisicaoId}/aprovacao` não possui corpo.

### 2. Cotação

`POST /api/v1/compras/requisicoes/{requisicaoId}/cotacoes`

```json
{
  "fornecedorId": "44444444-4444-4444-4444-444444444444",
  "validoAte": "2026-12-31T23:59:59Z",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "quantidade": 10,
      "custoUnitario": 4000.00
    }
  ]
}
```

Consulte `GET /api/v1/compras/requisicoes/{requisicaoId}/comparativo`. Guarde o `id` da cotação escolhida.

### 3. Pedido

`POST /api/v1/compras/cotacoes/{cotacaoId}/pedido`

```json
{
  "depositoId": "88888888-8888-8888-8888-888888888888",
  "numeroParcelas": 2,
  "primeiroVencimento": "2026-10-01"
}
```

### 4. Recebimentos parciais

Cada chamada precisa de um `Idempotency-Key` diferente. Repetir a mesma chave e o mesmo corpo retorna o recebimento já processado.

`POST /api/v1/compras/pedidos/{pedidoCompraId}/recebimentos`

```json
{
  "documentoFornecedor": "NF-2026-0001-A",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "quantidade": 4,
      "lote": "LOTE-FORN-01",
      "validadeLote": "2027-12-31"
    }
  ]
}
```

Segundo recebimento:

```json
{
  "documentoFornecedor": "NF-2026-0001-B",
  "itens": [
    {
      "skuId": "77777777-7777-7777-7777-777777777777",
      "quantidade": 6,
      "lote": "LOTE-FORN-02",
      "validadeLote": "2028-01-31"
    }
  ]
}
```

## Financeiro

`POST /api/v1/financeiro/contas`

```json
{
  "empresaId": "11111111-1111-1111-1111-111111111111",
  "filialId": "22222222-2222-2222-2222-222222222222",
  "codigo": "BANCO-02",
  "nome": "Conta corrente operacional",
  "saldoInicial": 0.00
}
```

Liste recebíveis:

```text
GET /api/v1/financeiro/titulos?empresaId={empresaId}&tipo=RECEBER&status=ABERTO&status=PARCIAL
```

`POST /api/v1/financeiro/titulos/{tituloId}/liquidacoes`, com `Idempotency-Key`:

```json
{
  "contaFinanceiraId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
  "valor": 4680.00,
  "formaPagamento": "PIX",
  "observacao": "Recebimento da primeira parcela"
}
```

`POST /api/v1/financeiro/liquidacoes/{liquidacaoId}/estornos`, com nova chave:

```json
{
  "motivo": "Pagamento conciliado na conta incorreta"
}
```

Formas aceitas: `DINHEIRO`, `PIX`, `CARTAO`, `BOLETO`, `TRANSFERENCIA` e `CHEQUE`.

## Relatórios e auditoria

```text
GET /api/v1/relatorios/resumo-gerencial?empresaId={empresaId}&filialId={filialId}&inicio=2026-08-01&fim=2026-08-31
GET /api/v1/relatorios/reposicao?empresaId={empresaId}&filialId={filialId}
GET /api/v1/relatorios/inadimplencia?empresaId={empresaId}&filialId={filialId}&referencia=2026-08-31
GET /api/v1/relatorios/exportacoes/gerencial.xlsx?empresaId={empresaId}&filialId={filialId}&inicio=2026-08-01&fim=2026-08-31
GET /api/v1/auditoria?empresaId={empresaId}&inicio=2026-08-01T00:00:00Z&fim=2026-09-01T00:00:00Z
```

## Formato de erro

Erros de negócio usam `application/problem+json` e incluem código estável e correlação:

```json
{
  "type": "https://commercecore.local/problemas/saldo_financeiro_insuficiente",
  "title": "Regra de negocio violada",
  "status": 422,
  "detail": "A conta financeira nao possui saldo suficiente para o pagamento.",
  "instance": "/api/v1/financeiro/titulos/00000000-0000-0000-0000-000000000000/liquidacoes",
  "codigo": "SALDO_FINANCEIRO_INSUFICIENTE",
  "correlacao": "teste-manual-001"
}
```
