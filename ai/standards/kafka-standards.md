# Kafka Standards

## Topic naming

`{domain}.{entity}.{event-type}`, all lowercase, dot-separated, past-tense
event type: `orders.order.cancelled`, `billing.invoice.paid`.

## Consumer group naming

`{service-name}.{purpose}`: `billing-service.order-cancelled-consumer`.

## Event payload

- Always a dedicated `record` type in `{module}/event/`, versioned
  intentionally — never reuse an internal JPA entity or DTO as the wire
  format (changing the entity would silently change the contract).
- Include: a stable unique event ID (`UUID`), the aggregate ID (used as the
  Kafka message key for per-aggregate ordering), an event timestamp, and only
  the fields consumers actually need.
- Schema changes are additive by default (new optional field). Removing or
  repurposing a field is a breaking change — treat it like a breaking API
  change (see `standards/api-standards.md` → backward compatibility).

## Publishing

- Publish **after** the originating database transaction commits — via
  `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` on an
  internal Spring application event, whose listener performs the actual Kafka
  publish. Never publish from inside the same transaction that might still
  roll back.
- One dedicated `{Entity}EventPublisher` `@Component` per aggregate — never
  call `KafkaTemplate` directly from a service method.

## Consuming

- One `@KafkaListener` per event type.
- **Idempotent by design** — Kafka guarantees at-least-once delivery, so
  every consumer checks whether an event ID was already processed (e.g. a
  `processed_events` table or a natural idempotency key on the resulting
  write) before applying side effects.
- Never call another module's service directly from a listener — go through
  that module's facade, same rule as any other inter-module call (see
  `standards/architecture-standards.md`).
- Failure handling: retry with backoff for transient errors (`RetryTemplate`
  / Spring Kafka's built-in retry support), then route to a dead-letter topic
  `{original-topic}.DLQ` after retries are exhausted. Never silently drop a
  message or infinite-loop on a poison message.

## Testing

- Use **Testcontainers Kafka** for integration tests — publish a real message
  and assert the expected side effect end-to-end, rather than unit-testing
  the listener method in isolation only.

## Observability

- Log (at INFO) every publish and every consume with the event ID and
  aggregate ID — never log the full payload if it may contain sensitive
  fields (cross-check `context/domain-glossary.md`).
- Consumer lag and DLQ depth should be visible in whatever monitoring the
  project uses — flag in review if a new consumer ships without any
  observability hook.
