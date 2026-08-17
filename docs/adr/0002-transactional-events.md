# ADR 0002: Eventos transacionais entre módulos

- Status: aceito
- Data: 2026-08-16

## Contexto

Faturamentos e recebimentos de compra precisam originar títulos financeiros. Uma chamada direta acoplaria o módulo operacional à implementação financeira; um evento somente em memória poderia ser perdido entre o commit e o consumidor.

## Decisão

Publicar eventos de domínio pelo Spring Modulith e persistir suas publicações no PostgreSQL. O financeiro consome eventos após o commit e registra o identificador processado para garantir idempotência adicional.

## Consequências

- o módulo de origem conhece apenas o contrato do evento;
- falhas do consumidor deixam publicação rastreável para reprocessamento;
- existe consistência eventual curta entre fatura/recebimento e título;
- operação e testes precisam observar publicações incompletas.
