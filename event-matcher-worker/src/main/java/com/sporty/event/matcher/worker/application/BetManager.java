package com.sporty.event.matcher.worker.application;

import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

/**
 * Provides bet-oriented application operations used by the matching workflow.
 */
public interface BetManager {

    /**
     * Loads a deterministic batch of bets for the supplied event and status.
     *
     * @param eventId event identifier whose bets should be loaded.
     * @param betStatus status that qualifying bets must currently have.
     * @param pageRequest page definition that limits the batch size.
     * @return ordered bets ready for matching or dispatch.
     */
    List<Bet> findByEventIdAndStatusOrderByIdAsc(UUID eventId, BetStatus betStatus, PageRequest pageRequest);

    /**
     * Claims candidate bets by transitioning them from one status to another.
     *
     * @param ids identifiers of the candidate bets to claim.
     * @param current status to assign to successfully claimed bets.
     * @param previous status that must still be present for the claim to succeed.
     * @return number of bets actually claimed.
     */
    int claim(List<UUID> ids, BetStatus current, BetStatus previous);

    /**
     * Reloads the supplied bets from persistence.
     *
     * @param ids identifiers of bets to fetch.
     * @return bets found for the supplied identifiers.
     */
    List<Bet> findAllById(List<UUID> ids);

    /**
     * Stores a newly registered bet.
     *
     * @param bet bet to persist.
     */
    void create(Bet bet);

}
