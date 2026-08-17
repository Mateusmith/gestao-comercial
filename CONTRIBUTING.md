# Contribuindo

## Fluxo

1. Abra ou associe uma issue descrevendo problema, regra e critério de aceite.
2. Crie uma branch curta: `feat/descricao`, `fix/descricao` ou `docs/descricao`.
3. Preserve os limites dos módulos e mantenha entidades/repositórios em `internal`.
4. Adicione testes proporcionais ao risco da mudança.
5. Execute `./mvnw verify` e o smoke test antes do pull request.
6. Explique decisões e possíveis impactos no pull request.

## Convenções

- Código, campos, mensagens e banco usam português; arquivos e pastas usam inglês.
- Commits seguem Conventional Commits, por exemplo `feat: adiciona conciliacao bancaria`.
- Dinheiro nunca usa `double`; utilize `BigDecimal` ou `Dinheiro`.
- Migrações aplicadas são imutáveis. Novas mudanças recebem uma nova versão Flyway.
- Operações repetíveis com efeito financeiro ou físico devem ser idempotentes.
- Dependências novas precisam resolver uma necessidade concreta.

## Definição de pronto

- regra de negócio e autorização verificadas;
- respostas e erros documentados no OpenAPI;
- testes verdes, sem acesso indevido entre módulos;
- migração reversível por estratégia operacional documentada;
- documentação e coleção Postman atualizadas quando o contrato mudar.
