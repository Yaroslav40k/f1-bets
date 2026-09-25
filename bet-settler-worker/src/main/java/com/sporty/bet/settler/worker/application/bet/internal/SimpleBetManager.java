package com.sporty.bet.settler.worker.application.bet.internal;

import com.sporty.bet.settler.worker.application.bet.BetManager;
import com.sporty.bet.settler.worker.application.bet.BetSettlementRepository;
import com.sporty.bet.settler.worker.application.bet.internal.model.BetSettlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implements the application bet manager by delegating persistence work to the repository port.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleBetManager implements BetManager {

    private final BetSettlementRepository repository;

    /**
     * Persists the settlement produced for a matched bet.
     *
     * @param betSettlement settlement outcome to store.
     */
    @Override
    public void saveSettlement(BetSettlement betSettlement) {
        repository.save(betSettlement);
        log.debug("Saved settlement [{}]", betSettlement);
    }

}
