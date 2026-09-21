package com.sporty.bet.settler.worker.application.bet.internal.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the final settlement decision produced for a single bet.
 *
 * @param betId identifier of the settled bet.
 * @param result settlement result derived from the event outcome.
 * @param payout amount associated with the settlement entry.
 */
public record BetSettlement(
    UUID betId,
    BetSettlementResult result,
    BigDecimal payout) {}
