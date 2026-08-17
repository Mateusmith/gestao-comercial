CREATE TABLE empresa (
    id UUID PRIMARY KEY,
    razao_social VARCHAR(160) NOT NULL,
    nome_fantasia VARCHAR(120) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_empresa_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_empresa_cnpj CHECK (cnpj ~ '^[0-9]{14}$')
);

CREATE TABLE filial (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    codigo VARCHAR(20) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    fuso_horario VARCHAR(60) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_filial_empresa_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT uq_filial_cnpj UNIQUE (cnpj),
    CONSTRAINT ck_filial_cnpj CHECK (cnpj ~ '^[0-9]{14}$')
);

CREATE INDEX idx_filial_empresa ON filial(empresa_id);

CREATE TABLE parceiro_comercial (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    tipo_pessoa VARCHAR(10) NOT NULL,
    nome_razao_social VARCHAR(160) NOT NULL,
    nome_fantasia VARCHAR(120),
    cpf_cnpj VARCHAR(14) NOT NULL,
    email VARCHAR(160),
    telefone VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_parceiro_empresa_documento UNIQUE (empresa_id, cpf_cnpj),
    CONSTRAINT ck_parceiro_tipo CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    CONSTRAINT ck_parceiro_documento CHECK (cpf_cnpj ~ '^[0-9]{11,14}$')
);

CREATE INDEX idx_parceiro_empresa_nome ON parceiro_comercial(empresa_id, nome_razao_social);

CREATE TABLE papel_parceiro (
    parceiro_id UUID NOT NULL REFERENCES parceiro_comercial(id) ON DELETE CASCADE,
    papel VARCHAR(30) NOT NULL,
    PRIMARY KEY (parceiro_id, papel),
    CONSTRAINT ck_papel_parceiro CHECK (papel IN ('CLIENTE', 'FORNECEDOR', 'TRANSPORTADORA'))
);

CREATE TABLE categoria_produto (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    nome VARCHAR(100) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_categoria_empresa_nome UNIQUE (empresa_id, nome)
);

CREATE TABLE produto (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL REFERENCES empresa(id),
    categoria_id UUID NOT NULL REFERENCES categoria_produto(id),
    codigo VARCHAR(40) NOT NULL,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_produto_empresa_codigo UNIQUE (empresa_id, codigo)
);

CREATE INDEX idx_produto_empresa_nome ON produto(empresa_id, nome);

CREATE TABLE sku (
    id UUID PRIMARY KEY,
    produto_id UUID NOT NULL REFERENCES produto(id) ON DELETE CASCADE,
    codigo VARCHAR(50) NOT NULL,
    codigo_barras VARCHAR(50),
    descricao_variacao VARCHAR(120),
    unidade_medida VARCHAR(20) NOT NULL,
    controla_lote BOOLEAN NOT NULL DEFAULT FALSE,
    aceita_fracionado BOOLEAN NOT NULL DEFAULT FALSE,
    estoque_minimo NUMERIC(19,3) NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_sku_codigo UNIQUE (codigo),
    CONSTRAINT uq_sku_codigo_barras UNIQUE (codigo_barras),
    CONSTRAINT ck_sku_unidade CHECK (unidade_medida IN ('UNIDADE', 'QUILOGRAMA', 'LITRO', 'METRO', 'CAIXA')),
    CONSTRAINT ck_sku_estoque_minimo CHECK (estoque_minimo >= 0)
);

CREATE INDEX idx_sku_produto ON sku(produto_id);

CREATE TABLE event_publication (
    id UUID NOT NULL PRIMARY KEY,
    completion_date TIMESTAMPTZ,
    event_type VARCHAR(512) NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    publication_date TIMESTAMPTZ NOT NULL,
    serialized_event TEXT NOT NULL,
    status VARCHAR(20),
    completion_attempts INTEGER,
    last_resubmission_date TIMESTAMPTZ
);

CREATE INDEX idx_event_publication_listener_event
    ON event_publication(listener_id, serialized_event);
