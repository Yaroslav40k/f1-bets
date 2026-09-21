package com.sporty.event.matcher.worker.usecase.bet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.sporty.event.matcher.worker.adapter.in.messaging.EventOutcomeMessage;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchEventOutcomeToBetsUseCaseTest {

    @Mock
    private DispatchBetsUseCase dispatchBetsUseCase;

    private MatchEventOutcomeToBetsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MatchEventOutcomeToBetsUseCase(dispatchBetsUseCase);
    }

    @Test
    void match_whenSingleBatchExhaustsPendingBets_dispatchesTwiceAndStops() {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Monza Grand Prix", eventWinnerId);
        var bet = bet(eventId, eventWinnerId);

        when(dispatchBetsUseCase.dispatchNextBatch(message))
                .thenReturn(List.of(bet))
                .thenReturn(List.of());

        useCase.match(message);

        verify(dispatchBetsUseCase, times(2)).dispatchNextBatch(message);
        verifyNoMoreInteractions(dispatchBetsUseCase);
    }

    @Test
    void match_whenMultipleFullBatchesExist_keepsDispatchingUntilBatchIsEmpty() {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Silverstone Grand Prix", eventWinnerId);
        var batchOne = List.of(bet(eventId, eventWinnerId), bet(eventId, eventWinnerId));
        var batchTwo = List.of(bet(eventId, eventWinnerId));

        when(dispatchBetsUseCase.dispatchNextBatch(message))
                .thenReturn(batchOne)
                .thenReturn(batchTwo)
                .thenReturn(List.of());

        useCase.match(message);

        verify(dispatchBetsUseCase, times(3)).dispatchNextBatch(message);
    }

    @Test
    void match_whenNoPendingBetsExist_dispatchesOnceAndStopsImmediately() {
        var eventId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Suzuka", UUID.randomUUID());

        when(dispatchBetsUseCase.dispatchNextBatch(message)).thenReturn(List.of());

        useCase.match(message);

        verify(dispatchBetsUseCase, times(1)).dispatchNextBatch(any());
        verify(dispatchBetsUseCase).dispatchNextBatch(eq(message));
    }

    private static Bet bet(UUID eventId, UUID eventWinnerId) {
        return new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                eventWinnerId,
                BigDecimal.valueOf(25),
                BetStatus.DISPATCHED);
    }
}
