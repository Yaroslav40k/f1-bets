package com.sporty.event.matcher.worker.application.bet.internal;

import com.sporty.event.matcher.worker.application.BetManager;
import com.sporty.event.matcher.worker.application.BetRepository;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Implements the application bet manager by delegating persistence work to the repository port.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleBetManager implements BetManager {

    private final BetRepository repository;

    /**
     * Delegates loading of the next candidate bet batch to the repository.
     *
     * @param eventId event identifier whose bets should be loaded.
     * @param betStatus status that qualifying bets must currently have.
     * @param pageRequest page definition that limits the batch size.
     * @return ordered bets ready for matching or dispatch.
     */
    @Override
    public List<Bet> findByEventIdAndStatusOrderByIdAsc(UUID eventId, BetStatus betStatus, PageRequest pageRequest) {
        return repository.findByEventIdAndStatusOrderByIdAsc(eventId, betStatus, pageRequest);
    }

    /**
     * Delegates the optimistic claim of candidate bets to the repository.
     *
     * @param ids identifiers of the candidate bets to claim.
     * @param current status to assign to successfully claimed bets.
     * @param previous status that must still be present for the claim to succeed.
     * @return number of bets actually claimed.
     */
    @Override
    public int claim(List<UUID> ids, BetStatus current, BetStatus previous) {
        return repository.claim(ids, current, previous);
    }

    /**
     * Delegates reloading of persisted bets to the repository.
     *
     * @param ids identifiers of bets to fetch.
     * @return bets found for the supplied identifiers.
     */
    @Override
    public List<Bet> findAllById(List<UUID> ids) {
        return repository.findAllById(ids);
    }

    /**
     * Delegates creation of a new bet to the repository.
     *
     * @param bet bet to persist.
     */
    @Override
    public void create(Bet bet) {
        repository.create(bet);
    }

}
