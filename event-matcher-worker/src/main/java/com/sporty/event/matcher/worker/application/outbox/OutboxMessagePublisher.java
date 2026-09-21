package com.sporty.event.matcher.worker.application.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;

/**
 * Publishes persisted outbox events to the configured broker.
 */
public interface OutboxMessagePublisher {

    /**
     * Delivers the supplied outbox event to the broker.
     *
     * @param outboxEvent outbox event to publish.
     */
    void publish(OutboxEvent outboxEvent);
}
