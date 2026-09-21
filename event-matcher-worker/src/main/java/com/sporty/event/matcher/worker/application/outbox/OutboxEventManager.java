package com.sporty.event.matcher.worker.application.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventCommand;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;

/**
 * Coordinates appending and dispatching transactional outbox events.
 */
public interface OutboxEventManager {

    /**
     * Appends a new outbox event inside the current business transaction.
     *
     * @param outboxEventCommand command describing the event to persist.
     */
    void append(OutboxEventCommand outboxEventCommand);

    /**
     * Dispatches the next pending outbox event, if any.
     *
     * @return {@code true} when an event was processed, otherwise {@code false}.
     */
    boolean dispatchNext();

    /**
     * Resolves the broker topic associated with the supplied payload type.
     *
     * @param ruleType payload type whose topic should be used.
     * @return configured topic name for the payload type.
     */
    String getTopicByPayloadType(OutboxPayloadType ruleType);

    /**
     * Returns the maximum number of outbox events a scheduler tick should dispatch.
     *
     * @return configured dispatch batch size.
     */
    int getBatchSize();

}
