package com.sporty.event.matcher.worker.application.outbox.model;

import java.util.UUID;

/**
 * Represents a request to append a new transactional outbox event.
 *
 * @param aggregateId identifier of the aggregate that produced the event.
 * @param topic broker topic to publish to after dispatch.
 * @param partitionKey key used to preserve ordering for related messages.
 * @param payload domain payload that will be serialized into the outbox row.
 */
public record OutboxEventCommand(
        UUID aggregateId,
        String topic,
        String partitionKey,
        Object payload) {
}
