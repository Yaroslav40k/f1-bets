package com.sporty.event.matcher.worker.adapter.in.job.outbox;

import com.sporty.event.matcher.worker.usecase.outbox.DispatchDomainEventsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers periodic dispatch of persisted outbox events to RocketMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
class OutboxScheduler {

    DispatchDomainEventsUseCase dispatchDomainEventsUseCase;

    /**
     * Dispatches the next configured batch of pending outbox events.
     */
    @Scheduled(fixedDelayString = "${event-matcher-worker.outbox.dispatch.fixed-delay}")
    void schedule() {
        dispatchDomainEventsUseCase.dispatch();
    }
}
