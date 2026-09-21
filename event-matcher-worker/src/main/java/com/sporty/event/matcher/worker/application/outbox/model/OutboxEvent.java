package com.sporty.event.matcher.worker.application.outbox.model;

import java.util.UUID;

/**
 * Represents a persisted outbox event that is ready to be published to RocketMQ.
 *
 * @param id identifier of the outbox row and broker message key.
 * @param aggregateId identifier of the domain aggregate that produced the event.
 * @param topic broker topic to publish to.
 * @param partitionKey key used to preserve ordering for related messages.
 * @param payload serialized JSON payload stored in the outbox table.
 * @param status current lifecycle status of the outbox event.
 */
public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        String topic,
        String partitionKey,
        String payload,
        OutboxEventStatus status) {
}