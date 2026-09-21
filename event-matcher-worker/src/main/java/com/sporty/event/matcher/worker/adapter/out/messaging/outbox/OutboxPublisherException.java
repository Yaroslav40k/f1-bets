package com.sporty.event.matcher.worker.adapter.out.messaging.outbox;

import java.util.UUID;

/**
 * Signals that a persisted outbox event could not be delivered to RocketMQ.
 */
public class OutboxPublisherException extends RuntimeException {

    private final static String MESSAGE = "Failed to publish outbox event_id: [%s] to topic: [%s]";

    public OutboxPublisherException(UUID event_id,
                                    String topic,
                                    Throwable cause) {
        super(String.format(MESSAGE, event_id, topic), cause);
    }

}
