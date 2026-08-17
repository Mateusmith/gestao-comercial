CREATE TABLE tabela_preco (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    nome VARCHAR(100) NOT NULL,
    vigente_de TIMESTAMPTZ NOT NULL,
    vigente_ate TIMESTAMPTZ,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_tabela_preco_vigencia CHECK (vigente_ate IS NULL OR vigente_ate > vigente_de)
);

CREATE INDEX idx_tabela_preco_contexto_vigencia
    ON tabela_preco(empresa_id, filial_id, vigente_de, vigente_ate) WHERE ativa;

CREATE TABLE item_tabela_preco (
    id UUID PRIMARY KEY,
    tabela_preco_id UUID NOT NULL REFERENCES tabela_preco(id) ON DELETE CASCADE,
    sku_id UUID NOT NULL REFERENCES sku(id),
    valor_venda NUMERIC(19,2) NOT NULL,
    custo_referencia NUMERIC(19,2) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_tabela_preco UNIQUE (tabela_preco_id, sku_id),
    CONSTRAINT ck_item_tabela_valores CHECK (valor_venda > 0 AND custo_referencia >= 0)
);

CREATE INDEX idx_item_tabela_preco_sku ON item_tabela_preco(sku_id, tabela_preco_id);

CREATE TABLE promocao (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID REFERENCES filial(id),
    sku_id UUID NOT NULL REFERENCES sku(id),
    nome VARCHAR(100) NOT NULL,
    codigo_cupom VARCHAR(40),
    tipo_desconto VARCHAR(20) NOT NULL,
    valor_desconto NUMERIC(19,4) NOT NULL,
    quantidade_minima NUMERIC(19,3) NOT NULL,
    inicio TIMESTAMPTZ NOT NULL,
    fim TIMESTAMPTZ NOT NULL,
    prioridade INTEGER NOT NULL DEFAULT 0,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_promocao_tipo CHECK (tipo_desconto IN ('PERCENTUAL', 'VALOR_FIXO')),
    CONSTRAINT ck_promocao_valor CHECK (
        valor_desconto > 0 AND (tipo_desconto <> 'PERCENTUAL' OR valor_desconto <= 100)
    ),
    CONSTRAINT ck_promocao_quantidade CHECK (quantidade_minima > 0),
    CONSTRAINT ck_promocao_periodo CHECK (fim > inicio)
);

CREATE UNIQUE INDEX uq_promocao_cupom_empresa
    ON promocao(empresa_id, upper(codigo_cupom)) WHERE codigo_cupom IS NOT NULL AND ativa;
CREATE INDEX idx_promocao_aplicacao ON promocao(empresa_id, filial_id, sku_id, inicio, fim) WHERE ativa;

CREATE TABLE deposito (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_deposito_filial_codigo UNIQUE (filial_id, codigo)
);

CREATE INDEX idx_deposito_empresa_filial ON deposito(empresa_id, filial_id);

CREATE TABLE saldo_estoque (
    id UUID PRIMARY KEY,
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    sku_id UUID NOT NULL REFERENCES sku(id),
    lote VARCHAR(60) NOT NULL DEFAULT 'SEM_LOTE',
    validade_lote DATE,
    saldo_fisico NUMERIC(19,3) NOT NULL DEFAULT 0,
    saldo_reservado NUMERIC(19,3) NOT NULL DEFAULT 0,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_saldo_deposito_sku_lote UNIQUE (deposito_id, sku_id, lote),
    CONSTRAINT ck_saldo_estoque_nao_negativo CHECK (
        saldo_fisico >= 0 AND saldo_reservado >= 0 AND saldo_reservado <= saldo_fisico
    )
);

CREATE INDEX idx_saldo_estoque_fefo ON saldo_estoque(deposito_id, sku_id, validade_lote);

CREATE TABLE reserva_estoque (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    sku_id UUID NOT NULL REFERENCES sku(id),
    lote VARCHAR(60) NOT NULL,
    quantidade NUMERIC(19,3) NOT NULL,
    tipo_origem VARCHAR(30) NOT NULL,
    origem_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_reserva_origem_sku_lote UNIQUE (tipo_origem, origem_id, sku_id, lote),
    CONSTRAINT ck_reserva_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_reserva_status CHECK (status IN ('ATIVA', 'CONSUMIDA', 'LIBERADA', 'EXPIRADA'))
);

CREATE INDEX idx_reserva_estoque_origem ON reserva_estoque(tipo_origem, origem_id, status);
CREATE INDEX idx_reserva_estoque_expiracao ON reserva_estoque(status, expira_em);

CREATE TABLE movimentacao_estoque (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    deposito_id UUID NOT NULL REFERENCES deposito(id),
    sku_id UUID NOT NULL REFERENCES sku(id),
    lote VARCHAR(60) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    quantidade NUMERIC(19,3) NOT NULL,
    saldo_fisico_anterior NUMERIC(19,3) NOT NULL,
    saldo_fisico_posterior NUMERIC(19,3) NOT NULL,
    saldo_reservado_anterior NUMERIC(19,3) NOT NULL,
    saldo_reservado_posterior NUMERIC(19,3) NOT NULL,
    tipo_origem VARCHAR(30) NOT NULL,
    origem_id UUID NOT NULL,
    justificativa VARCHAR(300) NOT NULL,
    realizado_por VARCHAR(120) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_movimentacao_quantidade CHECK (quantidade > 0)
);

CREATE INDEX idx_movimentacao_estoque_consulta
    ON movimentacao_estoque(deposito_id, sku_id, ocorrido_em DESC);
CREATE INDEX idx_movimentacao_estoque_origem
    ON movimentacao_estoque(tipo_origem, origem_id);

CREATE OR REPLACE FUNCTION bloquear_alteracao_ledger()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Registros de ledger sao imutaveis';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimentacao_estoque_imutavel
BEFORE UPDATE OR DELETE ON movimentacao_estoque
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_ledger();
