package com.sporty.event.matcher.worker.application.bet.internal.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a bet stored by the matcher worker and eligible for outcome matching.
 *
 * @param id identifier of the bet.
 * @param userId identifier of the bettor who placed the bet.
 * @param eventId identifier of the sporting event the bet belongs to.
 * @param eventMarketId identifier of the market within the event.
 * @param eventWinnerId identifier of the outcome selected by the bettor.
 * @param amount stake amount placed on the selection.
 * @param status matcher-side lifecycle status of the bet.
 */
public record Bet(
        UUID id,
        UUID userId,
        UUID eventId,
        UUID eventMarketId,
        UUID eventWinnerId,
        BigDecimal amount,
        BetStatus status) {

}
