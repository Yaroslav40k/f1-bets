package com.sporty.event.matcher.worker.usecase.bet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sporty.event.matcher.worker.adapter.in.messaging.EventOutcomeMessage;
import com.sporty.event.matcher.worker.application.BetManager;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetWorkUnit;
import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventCommand;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.IdGenerator;

class DispatchBetsUseCaseTest {

    private static final int BATCH_SIZE = 10;
    private static final String TOPIC = "bet-settlements";

    private final BetManager betManager = mock(BetManager.class);
    private final OutboxEventManager outboxEventManager = mock(OutboxEventManager.class);
    private final IdGenerator idGenerator = mock(IdGenerator.class);

    @Test
    void dispatchNextBatch_whenPendingBetsExist_appendsOutboxWorkUnitAndReturnsDispatchedBets() {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Monza Grand Prix", eventWinnerId);
        var pendingBetOne = bet(eventId, eventWinnerId, BetStatus.PENDING);
        var pendingBetTwo = bet(eventId, eventWinnerId, BetStatus.PENDING);
        var dispatchedBetOne = withStatus(pendingBetOne, BetStatus.DISPATCHED);
        var dispatchedBetTwo = withStatus(pendingBetTwo, BetStatus.DISPATCHED);
        var importId = UUID.randomUUID();
        var aggregateId = UUID.randomUUID();
        var useCase = createUseCase();

        when(betManager.findByEventIdAndStatusOrderByIdAsc(eq(eventId), eq(BetStatus.PENDING), any()))
                .thenReturn(List.of(pendingBetOne, pendingBetTwo));
        when(betManager.claim(
                List.of(pendingBetOne.id(), pendingBetTwo.id()),
                BetStatus.DISPATCHED,
                BetStatus.PENDING))
                .thenReturn(2);
        when(betManager.findAllById(List.of(pendingBetOne.id(), pendingBetTwo.id())))
                .thenReturn(List.of(dispatchedBetOne, dispatchedBetTwo));
        when(outboxEventManager.getTopicByPayloadType(OutboxPayloadType.BET_SETTLEMENT_WORK_UNIT))
                .thenReturn(TOPIC);
        when(idGenerator.generateId()).thenReturn(importId, aggregateId);

        var result = useCase.dispatchNextBatch(message);

        var outboxCommandCaptor = ArgumentCaptor.forClass(OutboxEventCommand.class);
        verify(outboxEventManager).append(outboxCommandCaptor.capture());
        var command = outboxCommandCaptor.getValue();
        var workUnit = (BetWorkUnit) command.payload();

        assertThat(result).containsExactly(dispatchedBetOne, dispatchedBetTwo);
        assertThat(command.aggregateId()).isEqualTo(aggregateId);
        assertThat(command.topic()).isEqualTo(TOPIC);
        assertThat(command.partitionKey()).isEqualTo(importId.toString());
        assertThat(command.payload()).isInstanceOf(BetWorkUnit.class);
        assertThat(workUnit.eventId()).isEqualTo(eventId);
        assertThat(workUnit.eventName()).isEqualTo("Monza Grand Prix");
        assertThat(workUnit.eventWinnerId()).isEqualTo(eventWinnerId);
        assertThat(workUnit.bets()).containsExactly(dispatchedBetOne, dispatchedBetTwo);
    }

    @Test
    void dispatchNextBatch_whenNoPendingBetsExist_returnsEmptyListWithoutClaimingOrAppending() {
        var eventId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Suzuka", UUID.randomUUID());
        var useCase = createUseCase();

        when(betManager.findByEventIdAndStatusOrderByIdAsc(eq(eventId), eq(BetStatus.PENDING), any()))
                .thenReturn(List.of());

        var result = useCase.dispatchNextBatch(message);

        assertThat(result).isEmpty();
        verify(betManager, never()).claim(anyList(), any(), any());
        verify(betManager, never()).findAllById(anyList());
        verify(outboxEventManager, never()).append(any());
    }

    @Test
    void dispatchNextBatch_whenOutboxAppendFails_propagatesException() {
        var eventId = UUID.randomUUID();
        var eventWinnerId = UUID.randomUUID();
        var message = new EventOutcomeMessage(eventId, "Silverstone", eventWinnerId);
        var pendingBet = bet(eventId, eventWinnerId, BetStatus.PENDING);
        var dispatchedBet = withStatus(pendingBet, BetStatus.DISPATCHED);
        var failure = new RuntimeException("outbox failure");
        var useCase = createUseCase();

        when(betManager.findByEventIdAndStatusOrderByIdAsc(eq(eventId), eq(BetStatus.PENDING), any()))
                .thenReturn(List.of(pendingBet));
        when(betManager.claim(List.of(pendingBet.id()), BetStatus.DISPATCHED, BetStatus.PENDING))
                .thenReturn(1);
        when(betManager.findAllById(List.of(pendingBet.id()))).thenReturn(List.of(dispatchedBet));
        when(outboxEventManager.getTopicByPayloadType(OutboxPayloadType.BET_SETTLEMENT_WORK_UNIT))
                .thenReturn(TOPIC);
        when(idGenerator.generateId()).thenReturn(UUID.randomUUID(), UUID.randomUUID());
        doThrow(failure).when(outboxEventManager).append(any());

        // The transactional workflow relies on append failures escaping so the claim can roll back.
        assertThatThrownBy(() -> useCase.dispatchNextBatch(message)).isSameAs(failure);
    }

    private DispatchBetsUseCase createUseCase() {
        var useCase = new DispatchBetsUseCase(betManager, outboxEventManager, idGenerator);
        ReflectionTestUtils.setField(useCase, "batchSize", BATCH_SIZE);
        return useCase;
    }

    private static Bet bet(UUID eventId, UUID eventWinnerId, BetStatus status) {
        return new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                eventWinnerId,
                BigDecimal.valueOf(25),
                status);
    }

    private static Bet withStatus(Bet bet, BetStatus status) {
        return new Bet(
                bet.id(),
                bet.userId(),
                bet.eventId(),
                bet.eventMarketId(),
                bet.eventWinnerId(),
                bet.amount(),
                status);
    }
}
