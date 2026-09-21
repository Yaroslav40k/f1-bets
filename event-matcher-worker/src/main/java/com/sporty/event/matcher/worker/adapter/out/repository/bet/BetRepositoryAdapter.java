package com.sporty.event.matcher.worker.adapter.out.repository.bet;

import com.sporty.event.matcher.worker.application.BetRepository;
import com.sporty.event.matcher.worker.application.bet.internal.model.Bet;
import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adapts the application bet repository port to Spring Data JDBC persistence.
 */
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BetRepositoryAdapter implements BetRepository {

    BetJdbcRepository jdbcRepository;
    BetEntityMapper mapper;

    /**
     * Loads a deterministic page of bets for the supplied event and status.
     *
     * @param eventId event identifier whose bets should be loaded.
     * @param betStatus matcher-side status that qualifying bets must have.
     * @param pageRequest page definition controlling batch size.
     * @return mapped bet models ready for application use.
     */
    //TODO think about selection with cursor (should be faster)
    @Override
    public List<Bet> findByEventIdAndStatusOrderByIdAsc(UUID eventId, BetStatus betStatus, PageRequest pageRequest) {
        return jdbcRepository.findByEventIdAndStatusOrderByIdAsc(eventId, betStatus, pageRequest)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    /**
     * Atomically claims candidate bets by transitioning them to a new status.
     *
     * @param ids identifiers of the candidate bets to claim.
     * @param current status to assign when the claim succeeds.
     * @param previous status each row must still have to be claimed.
     * @return number of rows that were successfully claimed.
     */
    @Override
    public int claim(List<UUID> ids, BetStatus current, BetStatus previous) {
        return jdbcRepository.claim(ids, current, previous);
    }

    /**
     * Reloads the supplied bets after a claim attempt to observe their persisted status.
     *
     * @param ids identifiers of bets to load.
     * @return mapped bets found for the supplied identifiers.
     */
    @Override
    public List<Bet> findAllById(List<UUID> ids) {
        return jdbcRepository.findAllById(ids)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    /**
     * Persists a newly registered bet with a generated identifier.
     *
     * @param bet bet payload received from the API layer.
     */
    @Override
    public void create(Bet bet) {
        BetEntity entity = mapper.toEntity(bet);
        entity.setId(UUID.randomUUID());
        jdbcRepository.save(entity);
    }

}
