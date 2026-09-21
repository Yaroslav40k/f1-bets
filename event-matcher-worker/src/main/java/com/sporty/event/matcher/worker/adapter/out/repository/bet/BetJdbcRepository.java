package com.sporty.event.matcher.worker.adapter.out.repository.bet;

import com.sporty.event.matcher.worker.application.bet.internal.model.BetStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Performs Spring Data JDBC reads and state-transition updates for bet rows.
 */
interface BetJdbcRepository extends ListCrudRepository<BetEntity, UUID> {

  /**
   * Loads the next page of bets for an event and status in deterministic identifier order.
   *
   * @param eventId event identifier to search by.
   * @param status current matcher-side status that rows must have.
   * @param pageable page definition limiting the batch size.
   * @return ordered bet rows eligible for matching or dispatch.
   */
  List<BetEntity> findByEventIdAndStatusOrderByIdAsc(
      UUID eventId, BetStatus status, Pageable pageable);

  @Modifying
  @Query("""
      UPDATE bets
         SET status = :dispatched
       WHERE id IN (:ids)
         AND status = :expected
      """)
  /**
   * Transitions candidate bets to the dispatched state only when they still have the expected state.
   *
   * @param ids bet identifiers to claim.
   * @param dispatched state to assign to successfully claimed rows.
   * @param expected state that must still be present for a row to be updated.
   * @return number of rows actually updated, which may be lower under concurrent claims.
   */
  int claim(
      @Param("ids") List<UUID> ids,
      @Param("dispatched") BetStatus dispatched,
      @Param("expected") BetStatus expected);
}
