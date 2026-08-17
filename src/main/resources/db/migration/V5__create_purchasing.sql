CREATE SEQUENCE requisicao_compra_numero_seq START WITH 1;
CREATE SEQUENCE cotacao_fornecedor_numero_seq START WITH 1;
CREATE SEQUENCE pedido_compra_numero_seq START WITH 1;
CREATE SEQUENCE recebimento_compra_numero_seq START WITH 1;

CREATE TABLE requisicao_compra (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    justificativa VARCHAR(300) NOT NULL,
    solicitada_por VARCHAR(120) NOT NULL,
    aprovada_por VARCHAR(120),
    aprovada_em TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_requisicao_compra_status CHECK (status IN ('SOLICITADA', 'APROVADA', 'CONVERTIDA', 'CANCELADA')),
    CONSTRAINT ck_requisicao_compra_aprovacao CHECK (
        status = 'SOLICITADA' OR status = 'CANCELADA' OR (aprovada_por IS NOT NULL AND aprovada_em IS NOT NULL)
    )
);

CREATE INDEX idx_requisicao_compra_contexto ON requisicao_compra(empresa_id, filial_id, status, criado_em DESC);

CREATE TABLE item_requisicao_compra (
    id UUID PRIMARY KEY,
    requisicao_id UUID NOT NULL REFERENCES requisicao_compra(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    codigo_sku VARCHAR(50) NOT NULL,
    nome_produto VARCHAR(160) NOT NULL,
    quantidade NUMERIC(19,3) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_requisicao_compra_sku UNIQUE (requisicao_id, sku_id),
    CONSTRAINT ck_item_requisicao_quantidade CHECK (quantidade > 0)
);

CREATE TABLE cotacao_fornecedor (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    requisicao_id UUID NOT NULL REFERENCES requisicao_compra(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    fornecedor_id UUID NOT NULL REFERENCES parceiro_comercial(id),
    fornecedor_nome VARCHAR(160) NOT NULL,
    valido_ate TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_cotacao_requisicao_fornecedor UNIQUE (requisicao_id, fornecedor_id),
    CONSTRAINT ck_cotacao_status CHECK (status IN ('RECEBIDA', 'SELECIONADA', 'REJEITADA', 'EXPIRADA')),
    CONSTRAINT ck_cotacao_total CHECK (valor_total > 0)
);

CREATE INDEX idx_cotacao_comparativo ON cotacao_fornecedor(requisicao_id, valor_total);

CREATE TABLE item_cotacao_fornecedor (
    id UUID PRIMARY KEY,
    cotacao_id UUID NOT NULL REFERENCES cotacao_fornecedor(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    quantidade NUMERIC(19,3) NOT NULL,
    custo_unitario NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_cotacao_sku UNIQUE (cotacao_id, sku_id),
    CONSTRAINT ck_item_cotacao_valores CHECK (quantidade > 0 AND custo_unitario > 0 AND subtotal > 0)
);

CREATE TABLE pedido_compra (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    requisicao_id UUID NOT NULL UNIQUE REFERENCES requisicao_compra(id),
    cotacao_id UUID NOT NULL UNIQUE REFERENCES cotacao_fornecedor(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    fornecedor_id UUID NOT NULL REFERENCES parceiro_comercial(id),
    fornecedor_nome VARCHAR(160) NOT NULL,
    status VARCHAR(25) NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    numero_parcelas INTEGER NOT NULL,
    primeiro_vencimento DATE NOT NULL,
    emitido_em TIMESTAMPTZ NOT NULL,
    concluido_em TIMESTAMPTZ,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedido_compra_status CHECK (
        status IN ('EMITIDO', 'PARCIALMENTE_RECEBIDO', 'RECEBIDO', 'CANCELADO')
    ),
    CONSTRAINT ck_pedido_compra_total CHECK (valor_total > 0),
    CONSTRAINT ck_pedido_compra_parcelas CHECK (numero_parcelas BETWEEN 1 AND 24)
);

CREATE INDEX idx_pedido_compra_contexto ON pedido_compra(empresa_id, filial_id, status, emitido_em DESC);

CREATE TABLE item_pedido_compra (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedido_compra(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    codigo_sku VARCHAR(50) NOT NULL,
    nome_produto VARCHAR(160) NOT NULL,
    quantidade_pedida NUMERIC(19,3) NOT NULL,
    quantidade_recebida NUMERIC(19,3) NOT NULL DEFAULT 0,
    custo_unitario NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_pedido_compra_sku UNIQUE (pedido_id, sku_id),
    CONSTRAINT ck_item_pedido_compra_quantidades CHECK (
        quantidade_pedida > 0 AND quantidade_recebida >= 0 AND quantidade_recebida <= quantidade_pedida
    ),
    CONSTRAINT ck_item_pedido_compra_valores CHECK (custo_unitario > 0 AND subtotal > 0)
);

CREATE TABLE recebimento_compra (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    pedido_id UUID NOT NULL REFERENCES pedido_compra(id),
    documento_fornecedor VARCHAR(50) NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    recebido_em TIMESTAMPTZ NOT NULL,
    chave_idempotencia VARCHAR(100) NOT NULL UNIQUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_recebimento_compra_total CHECK (valor_total > 0)
);

CREATE INDEX idx_recebimento_compra_pedido ON recebimento_compra(pedido_id, recebido_em);

CREATE TABLE item_recebimento_compra (
    id UUID PRIMARY KEY,
    recebimento_id UUID NOT NULL REFERENCES recebimento_compra(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    quantidade NUMERIC(19,3) NOT NULL,
    lote VARCHAR(60),
    validade_lote DATE,
    custo_unitario NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_item_recebimento_valores CHECK (
        quantidade > 0 AND custo_unitario > 0 AND subtotal > 0
    )
);

CREATE UNIQUE INDEX uq_item_recebimento_sku_lote
    ON item_recebimento_compra(recebimento_id, sku_id, coalesce(lote, 'SEM_LOTE'));
