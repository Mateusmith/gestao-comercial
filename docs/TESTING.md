# Estratégia de testes

## Pirâmide adotada

| Camada | Objetivo | Exemplos |
|---|---|---|
| unidade | validar regra isolada e rápida | dinheiro, CPF/CNPJ, parcelamento, estados, saldo |
| arquitetura | preservar os limites do monólito modular | ciclos, dependências permitidas, pacote `internal` |
| integração | validar persistência e restrições reais | Flyway, PostgreSQL 17, gatilho de auditoria |
| sistema | provar o processo empresarial completo | `scripts/smoke-test.ps1` contra API, Keycloak e banco |

## Executar com Java 21

```bash
./mvnw verify
```

No Windows:

```powershell
.\mvnw.cmd verify
```

O relatório JaCoCo é gerado em `target/site/jacoco/index.html`.

## Executar sem Java instalado

O comando abaixo roda Maven e Java 21 em um container. O socket do Docker é compartilhado para que o Testcontainers crie e remova o PostgreSQL descartável.

```powershell
docker run --rm `
  -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal `
  -v /var/run/docker.sock:/var/run/docker.sock `
  -v commercecore-maven:/root/.m2 `
  -v "${PWD}:/workspace" -w /workspace `
  maven:3.9.11-eclipse-temurin-21 mvn -B verify
```

## Smoke test

Suba o ambiente completo:

```powershell
docker compose up -d --build
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-test.ps1
```

O roteiro valida:

1. saúde e rejeição de acesso sem token;
2. tabela de preço, promoção, cupom e margem;
3. orçamento, envio, aceite e conversão;
4. reserva FEFO, faturamento e baixa física;
5. publicação de contas a receber e divisão exata;
6. recebimento financeiro e repetição idempotente;
7. requisição, aprovação, cotação e pedido de compra;
8. dois recebimentos parciais e fechamento do pedido;
9. entrada de estoque e contas a pagar;
10. pagamento, saldo de caixa e rejeição por insuficiência;
11. resumo gerencial, arquivo XLSX e auditoria.

O script usa identificadores únicos por execução e pode ser repetido sem duplicar operações protegidas pela mesma chave.

## Collection Postman via Newman

Com a stack em execução, rode a collection na rede interna do Compose:

```powershell
docker run --rm --network commercecore_default `
  -v "${PWD}/postman:/etc/newman" postman/newman:alpine `
  run /etc/newman/GestaoComercial.postman_collection.json `
  -e /etc/newman/GestaoComercial.postman_environment.json `
  --env-var base_url=http://api:8082 `
  --env-var keycloak_url=http://keycloak:8080
```

O roteiro executa 28 requisições e 28 asserções, encadeando os identificadores gerados em tempo real.

## Banco descartável

Os testes de integração usam `postgres:17.5-alpine` via Testcontainers. Nenhum H2 é usado, evitando diferenças de SQL, tipos, bloqueios e gatilhos em relação ao ambiente real.

## Critério de CI

Pull requests e pushes em `main` precisam concluir `mvn verify`. O CI publica o relatório JaCoCo como artefato para inspeção, mas a qualidade não é reduzida a uma porcentagem: regras financeiras, concorrência, integrações e arquitetura recebem testes direcionados ao risco.
