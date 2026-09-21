package com.sporty.event.matcher.worker.usecase.outbox;

import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dispatches pending transactional outbox events in bounded scheduler-driven batches.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class DispatchDomainEventsUseCase {

    OutboxEventManager outboxManager;

    /**
     * Publishes up to the configured batch size of pending outbox events.
     */
    public void dispatch() {
        int processed = 0;
        while (processed < outboxManager.getBatchSize() && outboxManager.dispatchNext()) {
            processed++;
        }

        if (processed > 0) {
            log.debug("Dispatched [{}] outbox event(s)", processed);
        }
    }
}
