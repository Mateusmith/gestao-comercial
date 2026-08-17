CREATE SEQUENCE titulo_financeiro_numero_seq START WITH 1;

CREATE TABLE conta_financeira (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    saldo NUMERIC(19,2) NOT NULL DEFAULT 0,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_conta_financeira_filial_codigo UNIQUE (filial_id, codigo),
    CONSTRAINT ck_conta_financeira_saldo CHECK (saldo >= 0)
);

CREATE INDEX idx_conta_financeira_contexto ON conta_financeira(empresa_id, filial_id);

CREATE TABLE titulo_financeiro (
    id UUID PRIMARY KEY,
    numero VARCHAR(40) NOT NULL UNIQUE,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    filial_id UUID NOT NULL REFERENCES filial(id),
    parceiro_id UUID NOT NULL REFERENCES parceiro_comercial(id),
    parceiro_nome VARCHAR(160) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    tipo_origem VARCHAR(30) NOT NULL,
    origem_id UUID NOT NULL,
    documento_origem VARCHAR(40) NOT NULL,
    parcela INTEGER NOT NULL,
    total_parcelas INTEGER NOT NULL,
    data_emissao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    valor_original NUMERIC(19,2) NOT NULL,
    saldo NUMERIC(19,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_titulo_origem_parcela UNIQUE (tipo_origem, origem_id, parcela),
    CONSTRAINT ck_titulo_tipo CHECK (tipo IN ('RECEBER', 'PAGAR')),
    CONSTRAINT ck_titulo_status CHECK (status IN ('ABERTO', 'PARCIAL', 'QUITADO', 'CANCELADO')),
    CONSTRAINT ck_titulo_parcelas CHECK (parcela BETWEEN 1 AND total_parcelas AND total_parcelas BETWEEN 1 AND 24),
    CONSTRAINT ck_titulo_valores CHECK (valor_original > 0 AND saldo >= 0 AND saldo <= valor_original)
);

CREATE INDEX idx_titulo_financeiro_consulta
    ON titulo_financeiro(empresa_id, tipo, status, data_vencimento);
CREATE INDEX idx_titulo_financeiro_parceiro
    ON titulo_financeiro(parceiro_id, data_vencimento);

CREATE TABLE liquidacao_financeira (
    id UUID PRIMARY KEY,
    titulo_id UUID NOT NULL REFERENCES titulo_financeiro(id),
    conta_financeira_id UUID NOT NULL REFERENCES conta_financeira(id),
    tipo VARCHAR(15) NOT NULL,
    liquidacao_original_id UUID REFERENCES liquidacao_financeira(id),
    valor NUMERIC(19,2) NOT NULL,
    forma_pagamento VARCHAR(20) NOT NULL,
    chave_idempotencia VARCHAR(100) NOT NULL UNIQUE,
    observacao VARCHAR(300),
    realizado_por VARCHAR(120) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_estorno_liquidacao UNIQUE (liquidacao_original_id, tipo),
    CONSTRAINT ck_liquidacao_tipo CHECK (tipo IN ('LIQUIDACAO', 'ESTORNO')),
    CONSTRAINT ck_liquidacao_valor CHECK (valor > 0),
    CONSTRAINT ck_liquidacao_forma CHECK (
        forma_pagamento IN ('PIX', 'BOLETO', 'CARTAO', 'DINHEIRO', 'TRANSFERENCIA', 'CHEQUE')
    ),
    CONSTRAINT ck_liquidacao_original CHECK (
        (tipo = 'LIQUIDACAO' AND liquidacao_original_id IS NULL)
        OR (tipo = 'ESTORNO' AND liquidacao_original_id IS NOT NULL)
    )
);

CREATE INDEX idx_liquidacao_titulo ON liquidacao_financeira(titulo_id, ocorrido_em);

CREATE TABLE movimentacao_financeira (
    id UUID PRIMARY KEY,
    conta_financeira_id UUID NOT NULL REFERENCES conta_financeira(id),
    liquidacao_id UUID NOT NULL UNIQUE REFERENCES liquidacao_financeira(id),
    tipo_titulo VARCHAR(10) NOT NULL,
    tipo_operacao VARCHAR(15) NOT NULL,
    valor NUMERIC(19,2) NOT NULL,
    saldo_anterior NUMERIC(19,2) NOT NULL,
    saldo_posterior NUMERIC(19,2) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_movimento_financeiro_tipo_titulo CHECK (tipo_titulo IN ('RECEBER', 'PAGAR')),
    CONSTRAINT ck_movimento_financeiro_tipo_operacao CHECK (tipo_operacao IN ('LIQUIDACAO', 'ESTORNO')),
    CONSTRAINT ck_movimento_financeiro_valor CHECK (valor > 0 AND saldo_anterior >= 0 AND saldo_posterior >= 0)
);

CREATE INDEX idx_movimentacao_financeira_conta
    ON movimentacao_financeira(conta_financeira_id, ocorrido_em DESC);

CREATE TABLE evento_financeiro_processado (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL UNIQUE,
    tipo_evento VARCHAR(100) NOT NULL,
    processado_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE TRIGGER trg_liquidacao_financeira_imutavel
BEFORE UPDATE OR DELETE ON liquidacao_financeira
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_ledger();

CREATE TRIGGER trg_movimentacao_financeira_imutavel
BEFORE UPDATE OR DELETE ON movimentacao_financeira
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_ledger();

CREATE TRIGGER trg_evento_financeiro_imutavel
BEFORE UPDATE OR DELETE ON evento_financeiro_processado
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_ledger();
