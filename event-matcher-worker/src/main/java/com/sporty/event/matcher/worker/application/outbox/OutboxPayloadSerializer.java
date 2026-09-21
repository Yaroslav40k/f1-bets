package com.sporty.event.matcher.worker.application.outbox;

/**
 * Serializes outbox payload objects before they are persisted.
 */
public interface OutboxPayloadSerializer {

    /**
     * Serializes the supplied payload object.
     *
     * @param payload domain payload to serialize.
     * @return serialized representation suitable for storage in the outbox.
     */
    String serialize(Object payload);
}
