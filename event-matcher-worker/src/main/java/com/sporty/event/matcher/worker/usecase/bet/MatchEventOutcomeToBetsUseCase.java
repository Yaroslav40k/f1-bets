package com.sporty.event.matcher.worker.usecase.bet;

import com.sporty.event.matcher.worker.adapter.in.messaging.EventOutcomeMessage;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Repeatedly dispatches pending bets for an outcome until no more matching bets remain.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchEventOutcomeToBetsUseCase {

    DispatchBetsUseCase dispatchBetsUseCase;

    /**
     * Matches the supplied event outcome against all pending bets for that event.
     *
     * @param message event outcome to process.
     */
    public void match(EventOutcomeMessage message) {
        int totalClaimed =0;
        int batchCount = 0;

        List<Bet> batch;

        while (!(batch = dispatchBetsUseCase.dispatchNextBatch(message)).isEmpty()) {
            totalClaimed += batch.size();
            batchCount++;
        }

        if (totalClaimed == 0) {
            log.debug("No pending bets for event [{}] - nothing to settle", message.eventId());
        } else {
            log.debug(
                    "Matched [{}] pending bet(s) for event [{}] across [{}] batch(es)",
                    totalClaimed,
                    message.eventId(),
                    batchCount);
        }

    }

}
