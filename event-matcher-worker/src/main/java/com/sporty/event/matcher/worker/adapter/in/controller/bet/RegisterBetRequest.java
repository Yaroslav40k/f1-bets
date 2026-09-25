package com.sporty.event.matcher.worker.adapter.in.controller.bet;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the request body accepted by the bet registration endpoint.
 *
 * <p>The bet's lifecycle status is deliberately absent: it is owned by the platform and always
 * starts as {@code PENDING}, so a caller must not be able to influence it.
 *
 * @param userId identifier of the bettor placing the bet.
 * @param eventId identifier of the sporting event being bet on.
 * @param eventMarketId identifier of the market within the event.
 * @param eventWinnerId identifier of the outcome the bettor is backing.
 * @param amount stake placed on the selection.
 */
public record RegisterBetRequest(
        @NotNull(message = "userId is required") UUID userId,
        @NotNull(message = "eventId is required") UUID eventId,
        @NotNull(message = "eventMarketId is required") UUID eventMarketId,
        // Required: this is the bettor's selection and it is what the settler compares against the
        // event winner. A bet without it could only ever be settled as LOST.
        @NotNull(message = "eventWinnerId is required") UUID eventWinnerId,
        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero") BigDecimal amount) {
}
