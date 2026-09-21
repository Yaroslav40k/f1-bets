package com.sporty.event.matcher.worker.application.bet.internal.model;

import java.util.List;
import java.util.UUID;

/**
 * Represents the settlement payload that the matcher writes to the transactional outbox.
 *
 * @param eventId identifier of the event whose outcome triggered the work unit.
 * @param eventName display name of the event for downstream traceability.
 * @param eventWinnerId identifier of the winning outcome used to grade the bets.
 * @param bets dispatched bets that must be settled downstream.
 */
public record BetWorkUnit(
        UUID eventId,
        String eventName,
        UUID eventWinnerId,
        List<Bet> bets
) {
}
