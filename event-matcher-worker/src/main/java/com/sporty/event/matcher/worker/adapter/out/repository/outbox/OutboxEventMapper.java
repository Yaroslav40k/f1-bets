package com.sporty.event.matcher.worker.adapter.out.repository.outbox;

import com.sporty.event.matcher.worker.application.outbox.model.OutboxEvent;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps domain outbox records to JDBC entities and stamps insert timestamps.
 */
@Mapper(componentModel = "spring", imports = LocalDateTime.class)
interface OutboxEventMapper {

    OutboxEvent toOutboxRecord(OutboxEventEntity entity);

    // Spring Data JDBC has no Hibernate-style @CreationTimestamp/@UpdateTimestamp - set both
    // explicitly here since this mapping only ever runs on initial insert (updates go through
    // the repository's dedicated markPublished/markFailed native queries).
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    OutboxEventEntity toEntity(OutboxEvent outboxEvent);
}
