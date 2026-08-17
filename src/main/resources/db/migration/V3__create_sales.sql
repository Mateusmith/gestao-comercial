CREATE SEQUENCE orcamento_venda_numero_seq START WITH 1;
CREATE SEQUENCE pedido_venda_numero_seq START WITH 1;
CREATE SEQUENCE fatura_venda_numero_seq START WITH 1;

CREATE TABLE orcamento_venda (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    cliente_id UUID NOT NULL REFERENCES parceiro_comercial(id),
    cliente_nome VARCHAR(160) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valido_ate TIMESTAMPTZ NOT NULL,
    codigo_cupom VARCHAR(40),
    valor_total NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_orcamento_status CHECK (
        status IN ('EM_EDICAO', 'ENVIADO', 'ACEITO', 'CONVERTIDO', 'CANCELADO', 'EXPIRADO')
    ),
    CONSTRAINT ck_orcamento_total CHECK (valor_total >= 0)
);

CREATE INDEX idx_orcamento_empresa_cliente ON orcamento_venda(empresa_id, cliente_id, criado_em DESC);

CREATE TABLE item_orcamento_venda (
    id UUID PRIMARY KEY,
    orcamento_id UUID NOT NULL REFERENCES orcamento_venda(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    codigo_sku VARCHAR(50) NOT NULL,
    nome_produto VARCHAR(160) NOT NULL,
    quantidade NUMERIC(19,3) NOT NULL,
    preco_unitario_base NUMERIC(19,2) NOT NULL,
    desconto_unitario NUMERIC(19,2) NOT NULL,
    preco_unitario_final NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    tabela_preco_id UUID NOT NULL REFERENCES tabela_preco(id),
    promocao_id UUID REFERENCES promocao(id),
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_orcamento_sku UNIQUE (orcamento_id, sku_id),
    CONSTRAINT ck_item_orcamento_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_orcamento_valores CHECK (
        preco_unitario_base >= 0 AND desconto_unitario >= 0
        AND preco_unitario_final >= 0 AND subtotal >= 0
    )
);

CREATE TABLE pedido_venda (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    orcamento_id UUID NOT NULL UNIQUE REFERENCES orcamento_venda(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    cliente_id UUID NOT NULL REFERENCES parceiro_comercial(id),
    cliente_nome VARCHAR(160) NOT NULL,
    status VARCHAR(20) NOT NULL,
    valor_total NUMERIC(19,2) NOT NULL,
    numero_parcelas INTEGER NOT NULL,
    primeiro_vencimento DATE NOT NULL,
    confirmado_em TIMESTAMPTZ,
    faturado_em TIMESTAMPTZ,
    cancelado_em TIMESTAMPTZ,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_pedido_venda_status CHECK (status IN ('RASCUNHO', 'CONFIRMADO', 'FATURADO', 'CANCELADO')),
    CONSTRAINT ck_pedido_venda_total CHECK (valor_total >= 0),
    CONSTRAINT ck_pedido_venda_parcelas CHECK (numero_parcelas BETWEEN 1 AND 24)
);

CREATE INDEX idx_pedido_venda_empresa_status ON pedido_venda(empresa_id, status, criado_em DESC);
CREATE INDEX idx_pedido_venda_cliente ON pedido_venda(cliente_id, criado_em DESC);

CREATE TABLE item_pedido_venda (
    id UUID PRIMARY KEY,
    pedido_id UUID NOT NULL REFERENCES pedido_venda(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    codigo_sku VARCHAR(50) NOT NULL,
    nome_produto VARCHAR(160) NOT NULL,
    quantidade NUMERIC(19,3) NOT NULL,
    preco_unitario NUMERIC(19,2) NOT NULL,
    desconto_unitario NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_pedido_venda_sku UNIQUE (pedido_id, sku_id),
    CONSTRAINT ck_item_pedido_venda_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_item_pedido_venda_valores CHECK (
        preco_unitario >= 0 AND desconto_unitario >= 0 AND subtotal >= 0
    )
);

CREATE TABLE fatura_venda (
    id UUID PRIMARY KEY,
    numero VARCHAR(30) NOT NULL UNIQUE,
    pedido_id UUID NOT NULL UNIQUE REFERENCES pedido_venda(id),
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    valor_total NUMERIC(19,2) NOT NULL,
    emitida_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_fatura_venda_total CHECK (valor_total > 0)
);

CREATE INDEX idx_fatura_venda_emissao ON fatura_venda(empresa_id, emitida_em DESC);
