package com.sporty.bet.settler.worker.application.transaction;

/**
 * Tracks processed settlement messages so the listener can handle redeliveries idempotently.
 */
public interface TransactionManager {

    /**
     * Checks whether a settlement work unit has already been processed.
     *
     * @param eventKey broker message key used as the idempotency key.
     * @return {@code true} when the message key was seen before, otherwise {@code false}.
     */
    boolean isDuplicate(String eventKey);
}
