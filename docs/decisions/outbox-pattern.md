# ADR: Transactional Outbox

## Decisão

O `order-service` persistirá pedidos e eventos de integração na mesma transação PostgreSQL. Um publisher agendado publicará posteriormente os eventos pendentes no Kafka.

## Motivo

A publicação direta no Kafka após o commit do pedido permite que o pedido seja salvo sem que o evento seja publicado.

## Consequências

- A publicação será assíncrona.
- A entrega será `at-least-once`.
- Consumidores deverão ser idempotentes.
- Eventos poderão permanecer pendentes durante indisponibilidade do Kafka.
- O publisher poderá publicar o mesmo evento mais de uma vez.

## Escopo inicial

- Uma instância do publisher.
- Polling com `@Scheduled`.
- Sem `SKIP LOCKED` no MVP.
- Evolução futura para múltiplas instâncias e locking distribuído.
