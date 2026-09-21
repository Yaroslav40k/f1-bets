package com.sporty.event.matcher.worker.usecase.bet;

import com.sporty.event.matcher.worker.adapter.in.messaging.EventOutcomeMessage;
import com.sporty.event.matcher.worker.application.BetManager;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetWorkUnit;
import com.sporty.event.matcher.worker.application.outbox.OutboxEventManager;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxEventCommand;
import com.sporty.event.matcher.worker.application.outbox.model.OutboxPayloadType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.UUID;

/**
 * Claims the next batch of pending bets for an event and appends a settlement work unit to the outbox.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DispatchBetsUseCase {

    static final int FIRST_PAGE = 0;

    final BetManager betManager;
    final OutboxEventManager outboxEventManager;
    final IdGenerator idGenerator;

    //TODO move to properties class
    @Value("${event-matcher-worker.batch.size}")
    int batchSize;

    /**
     * Claims one batch of pending bets for the supplied outcome and emits an outbox work unit.
     *
     * @param message event outcome that should be matched against pending bets.
     * @return bets that were successfully claimed and dispatched in this batch.
     */
    @Transactional
    public List<Bet> dispatchNextBatch(EventOutcomeMessage message) {
        var eventId = message.eventId();
        var pageRequest = PageRequest.of(FIRST_PAGE, batchSize);
        var linkedBets = betManager.findByEventIdAndStatusOrderByIdAsc(eventId, BetStatus.PENDING, pageRequest);
        if (linkedBets.isEmpty()) {
            return List.of();
        }

        var ids = linkedBets.stream().map(Bet::id).toList();
        int claimedBets = betManager.claim(ids, BetStatus.DISPATCHED, BetStatus.PENDING);
        checkForConcurrentClaims(eventId, claimedBets, ids);

        var dispatchedBets = betManager.findAllById(ids).stream()
                .filter(bet -> bet.status() == BetStatus.DISPATCHED)
                .toList();

        if(!dispatchedBets.isEmpty()) {
            var workUnit = new BetWorkUnit(eventId, message.eventName(), message.eventWinnerId(), dispatchedBets);
            UUID importId = idGenerator.generateId();
            outboxEventManager.append(new OutboxEventCommand(
                    idGenerator.generateId(),
                    outboxEventManager.getTopicByPayloadType(OutboxPayloadType.BET_SETTLEMENT_WORK_UNIT),
                    importId.toString(),
                    workUnit));
        }
        return dispatchedBets;
    }

    private static void checkForConcurrentClaims(UUID eventId, int claimedBets, List<UUID> ids) {
        if (claimedBets != ids.size()) {
            log.warn(
                    "Claimed [{}]/[{}] candidate bets for event [{}] - some were claimed concurrently",
                    claimedBets,
                    ids.size(),
                    eventId
            );
        }
    }
}
