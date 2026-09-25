package com.sporty.bet.settler.worker.usecase;

import com.sporty.bet.settler.worker.application.bet.internal.model.Bet;

import java.util.List;
import java.util.UUID;

public record SettleBetsCommand(UUID eventId,
                                String eventName,
                                UUID eventWinnerId,
                                List<Bet> bets) {
}
