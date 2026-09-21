---
name: kafka-event-integration
description: Add a Kafka producer and/or consumer for a domain event, following this repo's topic/schema/delivery conventions.
---

# Skill: Kafka Event Integration

See `ai/standards/kafka-standards.md` for the authoritative rules. This skill
is the step-by-step procedure.

## Producing an event

1. Define the event payload as a Java `record` in `{module}/event/` — never
   reuse an internal entity/DTO as the wire format.
2. Name the topic per convention: `{domain}.{entity}.{event-type}` (e.g.
   `orders.order.cancelled`), all lowercase, dot-separated.
3. Publish via a dedicated `{Entity}EventPublisher` `@Component` — never call
   `KafkaTemplate` directly from a service.
4. Publish AFTER the database transaction commits — use
   `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` on an
   internal Spring application event, whose listener then publishes to Kafka.
   This avoids publishing an event for a transaction that later rolls back.
5. Include a stable, unique event ID (`UUID`) and the aggregate ID as the
   Kafka message key — guarantees ordering per aggregate.
6. Register the new event/topic in `context/domain-glossary.md` (or a
   dedicated events index) so other teams/features can discover it.

## Consuming an event

1. One `@KafkaListener` per event type; consumer group name:
   `{service-name}.{purpose}` (e.g. `billing-service.order-cancelled-consumer`).
2. Handlers must be idempotent — check whether the event was already
   processed (e.g. by event ID in a processed-events table) before applying
   side effects, since Kafka guarantees at-least-once delivery.
3. On processing failure: retry with backoff for transient errors, then route
   to a dead-letter topic (`{original-topic}.DLQ`) — never silently drop or
   infinite-loop on a poison message.
4. Never call another module's service directly from a listener — go through
   the module's public facade/API, same rule as any other inter-module call.
5. Add a test using an embedded/test Kafka broker (e.g. Testcontainers Kafka)
   that publishes a message and asserts the expected side effect — do not
   rely on unit-testing the listener method in isolation only.

## Verification checklist

- [ ] Topic name follows convention
- [ ] Payload is a versioned `record`, not a reused entity/DTO
- [ ] Publish happens after commit (`AFTER_COMMIT`)
- [ ] Consumer is idempotent
- [ ] Dead-letter handling exists
- [ ] Integration test with a real (test) broker included
