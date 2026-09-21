package com.sporty.bet.settler.worker.application.bet.internal;

import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simulates the settlement persistence adapter by logging the computed result for each bet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleBetManager implements BetManager {

    /**
     * Logs the settlement that would normally be written to a durable store.
     *
     * @param betSettlement settlement outcome produced for a matched bet.
     */
    @Override
    public void saveSettlement(BetSettlement betSettlement) {
        //I mimic DB usage here.
        log.info("Saved settlement [{}]", betSettlement);
    }

}
