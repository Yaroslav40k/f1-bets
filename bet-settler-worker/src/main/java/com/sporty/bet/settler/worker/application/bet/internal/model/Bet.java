package com.sporty.bet.settler.worker.application.bet.internal.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a matched bet carried inside a settlement work unit.
 *
 * @param id identifier of the bet to settle.
 * @param userId identifier of the bettor who placed the bet.
 * @param eventId identifier of the sporting event the bet belongs to.
 * @param eventMarketId identifier of the market selected by the bettor.
 * @param eventWinnerId identifier of the runner or outcome backed by the bet.
 * @param amount stake amount that is echoed back for settlement processing.
 * @param status matcher-side status of the bet when the work unit was created.
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
