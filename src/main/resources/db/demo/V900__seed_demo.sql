INSERT INTO empresa (
    id, razao_social, nome_fantasia, cnpj, ativa, versao, criado_em, atualizado_em
) VALUES (
    '11111111-1111-1111-1111-111111111111', 'CommerceCore Demonstracao Ltda',
    'CommerceCore Demo', '11222333000181', true, 0, now(), now()
);

INSERT INTO filial (
    id, empresa_id, codigo, nome, cnpj, fuso_horario, ativa, versao, criado_em, atualizado_em
) VALUES (
    '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111',
    'MATRIZ', 'Matriz Sao Paulo', '45723174000110', 'America/Sao_Paulo', true, 0, now(), now()
);

INSERT INTO parceiro_comercial (
    id, empresa_id, tipo_pessoa, nome_razao_social, nome_fantasia, cpf_cnpj, email, telefone,
    ativo, versao, criado_em, atualizado_em
) VALUES
(
    '33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
    'FISICA', 'Mariana Souza', NULL, '52998224725', 'mariana@example.com', '11999990001',
    true, 0, now(), now()
),
(
    '44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111',
    'JURIDICA', 'TechSupply Distribuidora Ltda', 'TechSupply', '11444777000161',
    'vendas@techsupply.example', '1133334000', true, 0, now(), now()
);

INSERT INTO papel_parceiro (parceiro_id, papel) VALUES
    ('33333333-3333-3333-3333-333333333333', 'CLIENTE'),
    ('44444444-4444-4444-4444-444444444444', 'FORNECEDOR');

INSERT INTO categoria_produto (
    id, empresa_id, nome, ativa, versao, criado_em, atualizado_em
) VALUES (
    '55555555-5555-5555-5555-555555555555', '11111111-1111-1111-1111-111111111111',
    'Informatica', true, 0, now(), now()
);

INSERT INTO produto (
    id, empresa_id, categoria_id, codigo, nome, descricao, ativo, versao, criado_em, atualizado_em
) VALUES (
    '66666666-6666-6666-6666-666666666666', '11111111-1111-1111-1111-111111111111',
    '55555555-5555-5555-5555-555555555555', 'NOTEBOOK-PRO', 'Notebook Pro 14',
    'Notebook corporativo com 16 GB de RAM e SSD de 512 GB.', true, 0, now(), now()
);

INSERT INTO sku (
    id, produto_id, codigo, codigo_barras, descricao_variacao, unidade_medida,
    controla_lote, aceita_fracionado, estoque_minimo, ativo, versao, criado_em, atualizado_em
) VALUES (
    '77777777-7777-7777-7777-777777777777', '66666666-6666-6666-6666-666666666666',
    'NOTEBOOK-PRO-14-16-512', '7891234567895', '16 GB RAM / SSD 512 GB', 'UNIDADE',
    false, false, 10.000, true, 0, now(), now()
);

INSERT INTO tabela_preco (
    id, empresa_id, filial_id, nome, vigente_de, vigente_ate, ativa, versao, criado_em, atualizado_em
) VALUES (
    '99999999-9999-9999-9999-999999999999', '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222', 'Varejo Matriz', '2025-01-01T00:00:00Z',
    NULL, true, 0, now(), now()
);

INSERT INTO item_tabela_preco (
    id, tabela_preco_id, sku_id, valor_venda, custo_referencia, versao, criado_em, atualizado_em
) VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '99999999-9999-9999-9999-999999999999',
    '77777777-7777-7777-7777-777777777777', 5200.00, 3900.00, 0, now(), now()
);

INSERT INTO promocao (
    id, empresa_id, filial_id, sku_id, nome, codigo_cupom, tipo_desconto, valor_desconto,
    quantidade_minima, inicio, fim, prioridade, ativa, versao, criado_em, atualizado_em
) VALUES (
    'dddddddd-dddd-dddd-dddd-dddddddddddd', '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222', '77777777-7777-7777-7777-777777777777',
    'Boas-vindas 10%', 'BEMVINDO10', 'PERCENTUAL', 10.0000, 1.000,
    '2025-01-01T00:00:00Z', '2035-12-31T23:59:59Z', 10, true, 0, now(), now()
);

INSERT INTO deposito (
    id, empresa_id, filial_id, codigo, nome, ativo, versao, criado_em, atualizado_em
) VALUES (
    '88888888-8888-8888-8888-888888888888', '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222', 'PRINCIPAL', 'Deposito principal', true, 0, now(), now()
);

INSERT INTO saldo_estoque (
    id, deposito_id, sku_id, lote, validade_lote, saldo_fisico, saldo_reservado,
    versao, criado_em, atualizado_em
) VALUES (
    'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '88888888-8888-8888-8888-888888888888',
    '77777777-7777-7777-7777-777777777777', 'SEM_LOTE', NULL, 50.000, 0.000, 0, now(), now()
);

INSERT INTO movimentacao_estoque (
    id, empresa_id, filial_id, deposito_id, sku_id, lote, tipo, quantidade,
    saldo_fisico_anterior, saldo_fisico_posterior, saldo_reservado_anterior,
    saldo_reservado_posterior, tipo_origem, origem_id, justificativa, realizado_por,
    ocorrido_em, versao, criado_em, atualizado_em
) VALUES (
    'ffffffff-ffff-ffff-ffff-ffffffffffff', '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222', '88888888-8888-8888-8888-888888888888',
    '77777777-7777-7777-7777-777777777777', 'SEM_LOTE', 'AJUSTE_ENTRADA', 50.000,
    0.000, 50.000, 0.000, 0.000, 'AJUSTE', '12121212-1212-1212-1212-121212121212',
    'Carga inicial do ambiente de demonstracao.', 'seed-demo', now(), 0, now(), now()
);

INSERT INTO conta_financeira (
    id, empresa_id, filial_id, codigo, nome, saldo, ativa, versao, criado_em, atualizado_em
) VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222', 'BANCO-DEMO', 'Conta bancaria de demonstracao',
    0.00, true, 0, now(), now()
);
