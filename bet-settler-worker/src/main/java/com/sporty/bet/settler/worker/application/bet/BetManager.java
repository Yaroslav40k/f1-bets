package com.sporty.bet.settler.worker.application.bet;

import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;

/**
 * Persists or forwards completed bet settlements produced by the settler use case.
 */
public interface BetManager {

    /**
     * Stores a computed settlement for a single bet.
     *
     * @param betSettlement settlement outcome to persist or emit downstream.
     */
    void saveSettlement(BetSettlement betSettlement);
}
