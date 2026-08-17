# ADR 0001: Monólito modular como arquitetura inicial

- Status: aceito
- Data: 2026-08-16

## Contexto

Vendas, compras, estoque e financeiro possuem forte consistência transacional. Separá-los antecipadamente em serviços adicionaria rede, contratos distribuídos, sagas e operação independente antes de existir necessidade de escala por módulo.

## Decisão

Implantar uma única aplicação e um único PostgreSQL, organizando o código por capacidade de negócio. Spring Modulith declara e verifica dependências. Implementações de persistência permanecem em pacotes `internal`.

## Consequências

- transações entre agregados locais são simples e confiáveis;
- desenvolvimento, teste e demonstração exigem menos infraestrutura;
- módulos podem evoluir com limites explícitos;
- escala e implantação são compartilhadas;
- uma futura extração exige substituir contratos internos por contratos remotos, mas os limites atuais reduzem esse custo.
