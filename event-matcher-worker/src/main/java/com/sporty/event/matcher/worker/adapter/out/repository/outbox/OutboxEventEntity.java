package com.sporty.event.matcher.worker.adapter.out.repository.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Represents a row in the transactional {@code outbox_event} table.
 * The entity always reports itself as new so Spring Data JDBC inserts rows with preassigned
 * identifiers, while later state transitions are handled by dedicated SQL updates.
 */
@Table("outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class OutboxEventEntity implements Persistable<UUID> {

    @Id
    @Column("id")
    private UUID id;

    @Column("aggregate_id")
    private UUID aggregateId;

    @Column("topic")
    private String topic;

    @Column("partition_key")
    private String partitionKey;

    // Column type (CLOB/TEXT) is defined in schema.sql - no length limit here, unlike JPA's
    // implicit varchar(255) default, so a full JSON payload always fits.
    @Column("payload")
    private String payload;

    @Column("status")
    private OutboxEventStatus status;

    @Column("retry_count")
    private Integer retryCount = 0;

    @Column("published_at")
    private LocalDateTime publishedAt;

    @Column("last_error")
    private String lastError;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // save() in this codebase is only ever used to insert a brand-new event (with a
    // client-generated UUID assigned beforehand) - Spring Data JDBC's default "is new" check
    // would otherwise treat a non-null id as an existing row and issue an UPDATE that matches
    // zero rows. All later mutations (markPublished/markFailed) go through dedicated native
    // UPDATE queries, never through save(), so it is safe to always report "new" here.
    /**
     * Forces Spring Data JDBC to insert this outbox row even when the identifier is already set.
     *
     * @return always {@code true} because outbox events are created with generated UUIDs.
     */
    @Override
    public boolean isNew() {
        return true;
    }
}
