package com.sporty.event.matcher.worker.application.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for transactional outbox events.
 */
public interface OutboxEventRepository {

    /**
     * Stores a newly created outbox event.
     *
     * @param outboxEvent outbox event to persist.
     */
    void save(OutboxEvent outboxEvent);

    /**
     * Fetches the next pending outbox event that is still eligible for retry.
     *
     * @param maxRetries maximum retry count allowed for a pending event.
     * @return next pending outbox event when one is available.
     */
    Optional<OutboxEvent> fetchNextPending(int maxRetries);

    /**
     * Marks the supplied outbox event as published.
     *
     * @param id identifier of the outbox event to update.
     */
    void markPublished(UUID id);

    /**
     * Marks the supplied outbox event as failed and stores the failure reason.
     *
     * @param id identifier of the outbox event to update.
     * @param error publication failure message to persist.
     */
    void markFailed(UUID id, String error);
}
