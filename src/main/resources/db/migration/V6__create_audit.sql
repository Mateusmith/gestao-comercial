CREATE TABLE auditoria_operacao (
    id UUID PRIMARY KEY,
    empresa_id UUID REFERENCES empresa(id),
    ator_id VARCHAR(120) NOT NULL,
    metodo_http VARCHAR(10) NOT NULL,
    caminho VARCHAR(300) NOT NULL,
    parametros VARCHAR(1000),
    status_http INTEGER NOT NULL,
    endereco_ip VARCHAR(64) NOT NULL,
    correlacao VARCHAR(100) NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_auditoria_empresa_data ON auditoria_operacao(empresa_id, ocorrido_em DESC);
CREATE INDEX idx_auditoria_ator_data ON auditoria_operacao(ator_id, ocorrido_em DESC);
CREATE INDEX idx_auditoria_correlacao ON auditoria_operacao(correlacao);

CREATE TRIGGER trg_auditoria_operacao_imutavel
BEFORE UPDATE OR DELETE ON auditoria_operacao
FOR EACH ROW EXECUTE FUNCTION bloquear_alteracao_ledger();
