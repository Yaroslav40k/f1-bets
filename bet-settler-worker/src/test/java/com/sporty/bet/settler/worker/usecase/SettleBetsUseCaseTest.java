package com.sporty.bet.settler.worker.usecase;

import com.sporty.bet.settler.worker.adapter.in.messaging.BetWorkUnitMessage;
import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlementResult;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetStatus;
import com.sporty.bet.settler.worker.application.transaction.TransactionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettleBetsUseCaseTest {

    @Mock
    BetManager betManager;

    @Mock
    TransactionManager transactionManager;

    SettleBetsUseCase settleBetsUseCase;

    @BeforeEach
    void setUp() {
        settleBetsUseCase = new SettleBetsUseCase(betManager, transactionManager);
    }

    @Test
    void settleBets_whenMessageIsNew_savesWonAndLostSettlements() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var winningBet = bet(eventId, unitWinnerId, new BigDecimal("10.50"));
        var losingBet = bet(eventId, UUID.randomUUID(), new BigDecimal("25.00"));
        var unit = workUnit(eventId, unitWinnerId, winningBet, losingBet);
        var eventKey = UUID.randomUUID().toString();
        var settlementsCaptor = ArgumentCaptor.forClass(BetSettlement.class);

        when(transactionManager.isDuplicate(eventKey)).thenReturn(false);

        settleBetsUseCase.settleBets(eventKey, unit);

        verify(betManager, org.mockito.Mockito.times(2)).saveSettlement(settlementsCaptor.capture());
        assertThat(settlementsCaptor.getAllValues()).containsExactly(
                new BetSettlement(winningBet.id(), BetSettlementResult.WON, winningBet.amount()),
                new BetSettlement(losingBet.id(), BetSettlementResult.LOST, losingBet.amount())
        );
    }

    @Test
    void settleBets_whenMessageIsDuplicate_doesNotSaveAnySettlement() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var unit = workUnit(eventId, unitWinnerId, bet(eventId, unitWinnerId, new BigDecimal("10.50")));
        var eventKey = UUID.randomUUID().toString();

        when(transactionManager.isDuplicate(eventKey)).thenReturn(true);

        settleBetsUseCase.settleBets(eventKey, unit);

        verify(betManager, never()).saveSettlement(any(BetSettlement.class));
    }

    @Test
    void settleBets_whenSavingFirstSettlementFails_propagatesExceptionAndStopsProcessing() {
        var eventId = UUID.randomUUID();
        var unitWinnerId = UUID.randomUUID();
        var firstBet = bet(eventId, unitWinnerId, new BigDecimal("10.50"));
        var secondBet = bet(eventId, UUID.randomUUID(), new BigDecimal("25.00"));
        var unit = workUnit(eventId, unitWinnerId, firstBet, secondBet);
        var eventKey = UUID.randomUUID().toString();
        var failure = new RuntimeException("save failed");

        when(transactionManager.isDuplicate(eventKey)).thenReturn(false);
        doThrow(failure).when(betManager).saveSettlement(any(BetSettlement.class));

        assertThatThrownBy(() -> settleBetsUseCase.settleBets(eventKey, unit))
                .isSameAs(failure);

        verify(betManager, atMostOnce()).saveSettlement(any(BetSettlement.class));
    }

    private static BetWorkUnitMessage workUnit(UUID eventId, UUID unitWinnerId, Bet... bets) {
        return new BetWorkUnitMessage(eventId, "Monaco Grand Prix", unitWinnerId, List.of(bets));
    }

    private static Bet bet(UUID eventId, UUID betWinnerId, BigDecimal amount) {
        return new Bet(
                UUID.randomUUID(),
                UUID.randomUUID(),
                eventId,
                UUID.randomUUID(),
                betWinnerId,
                amount,
                BetStatus.DISPATCHED
        );
    }
}
