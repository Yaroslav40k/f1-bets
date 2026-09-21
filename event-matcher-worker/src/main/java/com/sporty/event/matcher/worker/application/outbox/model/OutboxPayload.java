package com.sporty.event.matcher.worker.application.outbox.model;

/**
 * Wraps a typed outbox payload together with its routing type.
 *
 * @param <T> payload body type.
 * @param eventType routing type used to resolve the destination topic.
 * @param payload payload body to serialize and publish.
 */
public record OutboxPayload<T>(
    OutboxPayloadType eventType,
    T payload
) {

}
