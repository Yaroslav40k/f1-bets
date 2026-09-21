package com.sporty.event.matcher.worker.application.bet.internal.model;

/**
 * Lifecycle of a bet as tracked by the matcher (this service).
 *
 * <pre>
 *   PENDING -&gt; DISPATCHED
 * </pre>
 *
 * <p>{@link #PENDING} means not yet matched to an outcome. {@link #DISPATCHED} means the matcher
 * has claimed this bet, graded it, and handed a settlement instruction to the outbox — it is no
 * longer visible to matching. The matcher's job ends here: whether the bet was actually WON,
 * LOST or VOID is decided by {@code BetGrader} and carried on the message, but that final
 * grading, and the "settle" step itself, is the separate {@code bet-settler} service's
 * responsibility, not this service's — it does not share this database or this enum, only the
 * {@code contracts} DTOs on the wire. See {@code Bets#claimBatch} for how the {@code
 * PENDING -&gt; DISPATCHED} transition is made idempotent under at-least-once redelivery.
 */
public enum BetStatus {
  PENDING,
  DISPATCHED
}

