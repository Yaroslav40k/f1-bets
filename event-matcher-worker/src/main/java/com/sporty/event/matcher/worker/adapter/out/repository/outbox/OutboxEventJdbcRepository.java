package com.sporty.event.matcher.worker.adapter.out.repository.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Performs Spring Data JDBC reads and updates for transactional outbox rows.
 */
@Repository
interface OutboxEventJdbcRepository extends CrudRepository<OutboxEventEntity, UUID> {

    // FOR UPDATE takes a row lock for the duration of the enclosing @Transactional method,
    // so two scheduler ticks can never both claim the same pending event.
    @Query("""
        SELECT * FROM outbox_event
         WHERE status = :status
           AND retry_count < :maxRetries
         ORDER BY created_at, id
         LIMIT :limit
         FOR UPDATE
        """)
    /**
     * Locks the next pending outbox rows so only one dispatcher can publish them.
     *
     * @param status status that qualifying outbox rows must have.
     * @param maxRetries maximum retry count allowed for eligible rows.
     * @param limit maximum number of rows to fetch in one call.
     * @return locked outbox rows ordered for deterministic dispatch.
     */
    List<OutboxEventEntity> fetchNextPending(
            @Param("status") OutboxEventStatus status,
            @Param("maxRetries") int maxRetries,
            @Param("limit") int limit);

    @Modifying
    @Query("""
        UPDATE outbox_event
           SET status = :status,
               published_at = CURRENT_TIMESTAMP
         WHERE id = :id
        """)
    /**
     * Marks an outbox row as published after the broker acknowledges delivery.
     *
     * @param id identifier of the published outbox row.
     * @param status status to persist for the row, typically {@code PUBLISHED}.
     */
    void markPublished(
            @Param("id") UUID id,
            @Param("status") OutboxEventStatus status);

    @Modifying
    @Query("""
        UPDATE outbox_event
           SET retry_count = retry_count + 1,
               last_error = :error
         WHERE id = :id
        """)
    /**
     * Records a failed publication attempt and increments the retry counter.
     *
     * @param id identifier of the outbox row that failed to publish.
     * @param error truncated broker or serialization error message.
     */
    void markFailed(@Param("id") UUID id, @Param("error") String error);
}
