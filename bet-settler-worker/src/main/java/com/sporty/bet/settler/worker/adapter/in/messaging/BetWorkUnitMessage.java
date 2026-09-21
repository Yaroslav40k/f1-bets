package com.sporty.bet.settler.worker.adapter.in.messaging;

import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;

import java.util.List;
import java.util.UUID;

/**
 * Represents the RocketMQ payload that instructs the settler to settle a batch of bets.
 *
 * @param eventId identifier of the sporting event whose outcome is being settled.
 * @param eventName display name of the event, used for downstream context and logging.
 * @param eventWinnerId identifier of the winning market outcome for the event.
 * @param bets bets that were matched for the event and now require settlement.
 */
public record BetWorkUnitMessage(
        UUID eventId,
        String eventName,
        UUID eventWinnerId,
        List<Bet> bets
) {
}
