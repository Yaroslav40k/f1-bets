package com.sporty.event.matcher.worker.adapter.out.repository.outbox;

import com.sporty.event.matcher.worker.application.outbox.OutboxEventRepository;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapts the outbox persistence port to Spring Data JDBC repositories.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    OutboxEventJdbcRepository jdbcRepository;
    OutboxEventMapper mapper;

    /**
     * Persists a newly created outbox event inside the current transaction.
     *
     * @param outboxEvent outbox event to store.
     */
    @Override
    public void save(OutboxEvent outboxEvent) {
        var outboxEventEntity = mapper.toEntity(outboxEvent);
        jdbcRepository.save(outboxEventEntity);
    }

    /**
     * Fetches the next pending outbox event that is still eligible for retry.
     *
     * @param maxRetries maximum retry count allowed for a pending event.
     * @return next pending outbox event when one is available.
     */
    @Override
    public Optional<OutboxEvent> fetchNextPending(int maxRetries) {
        return jdbcRepository.fetchNextPending(OutboxEventStatus.PENDING, maxRetries, 1).stream()
                .findFirst()
                .map(mapper::toOutboxRecord);
    }

    /**
     * Marks the given outbox event as successfully published.
     *
     * @param id identifier of the outbox event to update.
     */
    @Override
    public void markPublished(UUID id) {
        jdbcRepository.markPublished(id, OutboxEventStatus.PUBLISHED);
    }

    /**
     * Marks the given outbox event as failed and stores a bounded error message.
     *
     * @param id identifier of the outbox event to update.
     * @param error failure message produced while publishing the event.
     */
    @Override
    public void markFailed(UUID id, String error) {
        jdbcRepository.markFailed(id, truncateError(error));
    }

    private static String truncateError(String error) {
        return error != null && error.length() > 1024
                ? error.substring(0, 1024)
                : error;
    }
}
