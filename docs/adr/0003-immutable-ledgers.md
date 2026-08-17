# ADR 0003: Razões físicos, financeiros e de auditoria imutáveis

- Status: aceito
- Data: 2026-08-16

## Contexto

Sobrescrever saldo ou apagar um pagamento remove evidências necessárias para conciliação e investigação. Somente armazenar o estado atual não explica como ele foi obtido.

## Decisão

Tratar movimentos de estoque, movimentos de caixa e auditorias como registros append-only. Correções financeiras são lançamentos de estorno vinculados ao original. Saldos são atualizados transacionalmente, mas sempre reconciliáveis com o razão. A tabela de auditoria possui gatilho que bloqueia `UPDATE` e `DELETE`.

## Consequências

- cada alteração relevante pode ser explicada e conciliada;
- estornos preservam o histórico;
- relatórios históricos possuem fonte confiável;
- retenção e arquivamento precisam ser planejados quando o volume crescer.
