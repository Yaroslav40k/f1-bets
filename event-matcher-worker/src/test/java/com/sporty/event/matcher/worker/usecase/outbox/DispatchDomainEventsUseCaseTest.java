package com.sporty.event.matcher.worker.usecase.outbox;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DispatchDomainEventsUseCaseTest {

    @Mock
    private OutboxEventManager outboxManager;

    private DispatchDomainEventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DispatchDomainEventsUseCase(outboxManager);
    }

    @Test
    void dispatch_whenFewerPendingEventsThanBatchSize_stopsAsSoonAsQueueIsDrained() {
        when(outboxManager.getBatchSize()).thenReturn(10);
        when(outboxManager.dispatchNext()).thenReturn(true, true, false);

        useCase.dispatch();

        verify(outboxManager, times(3)).dispatchNext();
    }

    @Test
    void dispatch_whenPendingEventsExactlyFillBatchSize_stopsAtBatchSizeWithoutCallingDispatchNextAgain() {
        when(outboxManager.getBatchSize()).thenReturn(2);
        when(outboxManager.dispatchNext()).thenReturn(true, true, true);

        useCase.dispatch();

        verify(outboxManager, times(2)).dispatchNext();
    }

    @Test
    void dispatch_whenNoPendingEventsExist_callsDispatchNextOnceThenStops() {
        when(outboxManager.getBatchSize()).thenReturn(10);
        when(outboxManager.dispatchNext()).thenReturn(false);

        useCase.dispatch();

        verify(outboxManager, times(1)).dispatchNext();
    }
}
