package com.sporty.bet.settler.worker.usecase;

import com.sporty.bet.settler.worker.adapter.in.messaging.BetWorkUnitMessage;
import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlementResult;
import com.sporty.bet.settler.worker.application.transaction.TransactionManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

/**
 * Settles every bet in a work unit and skips duplicate broker deliveries by message key.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SettleBetsUseCase {

    BetManager betManager;
    TransactionManager transactionManager;

    /**
     * Settles all bets contained in the supplied work unit unless the message was already seen.
     *
     * @param eventKey broker message key used as the idempotency key for the work unit.
     * @param unit matched bets plus the outcome they should be settled against.
     */
    @Transactional
    public void settleBets(String eventKey, BetWorkUnitMessage unit) {
        if (transactionManager.isDuplicate(eventKey)) {
            log.info("Skipping duplicate delivery of settlement unit [{}] for event [{}]", eventKey, unit.eventId());
            return;
        }

        var settledBets = unit.bets()
                .stream()
                .map(settleBet(unit));
        settledBets.forEach(betManager::saveSettlement);
        log.info("Settled [{}] bets for event [{}]", unit.bets().size(), unit.eventId());
    }

    private static @NonNull Function<Bet, BetSettlement> settleBet(BetWorkUnitMessage unit) {
        return bet -> {
            BetSettlementResult result;
            if (unit.eventWinnerId().equals(bet.eventWinnerId())) {
                result = BetSettlementResult.WON;
            } else {
                result = BetSettlementResult.LOST;
            }
            return new BetSettlement(bet.id(), result, bet.amount());
        };
    }

}
