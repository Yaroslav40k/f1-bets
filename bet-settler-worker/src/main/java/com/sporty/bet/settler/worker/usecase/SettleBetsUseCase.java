package com.sporty.bet.settler.worker.usecase;

import com.sporty.bet.settler.worker.adapter.in.messaging.BetWorkUnitMessage;
import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlementResult;
import com.sporty.bet.settler.worker.application.idempotency.DuplicateMessageException;
import com.sporty.bet.settler.worker.application.idempotency.IdempotencyManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

/**
 * Settles every bet in a work unit exactly once, even when the broker redelivers the message.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SettleBetsUseCase {

    BetManager betManager;
    IdempotencyManager idempotencyManager;

    /**
     * Claims the work unit and settles all of its bets within a single transaction.
     *
     * <p>The claim is written <em>before</em> the settlements but commits <em>with</em> them: the
     * insert into the idempotency ledger is what rejects a concurrent duplicate, while the shared
     * transaction guarantees that a failure part-way through settlement rolls the claim back too.
     * A redelivery of a failed work unit is therefore reprocessed rather than silently skipped.
     *
     * @param eventKey broker message key used as the idempotency key for the work unit.
     * @param command matched bets plus the outcome they should be settled against.
     * @throws DuplicateMessageException if this work unit was already settled.
     */
    @Transactional
    public void settleBets(String eventKey, SettleBetsCommand command) {
        idempotencyManager.claim(eventKey);

        command.bets()
                .stream()
                .map(settleBet(command))
                .forEach(betManager::saveSettlement);

        log.info("Settled [{}] bet(s) for event [{}] from unit [{}]",
                command.bets().size(), command.eventId(), eventKey);
    }

    private static @NonNull Function<Bet, BetSettlement> settleBet(SettleBetsCommand command) {
        return bet -> {
            BetSettlementResult result;
            if (command.eventWinnerId().equals(bet.eventWinnerId())) {
                result = BetSettlementResult.WON;
            } else {
                result = BetSettlementResult.LOST;
            }
            return new BetSettlement(bet.id(), result, bet.amount());
        };
    }

}
