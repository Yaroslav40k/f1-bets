package com.sporty.bet.settler.worker.application.transaction.internal;

import com.sporty.bet.settler.worker.application.transaction.TransactionManager;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

/**
 * Keeps idempotency keys in memory for the lifetime of a single worker process.
 * The implementation is intentionally lightweight for the take-home exercise and protects only
 * against duplicate deliveries seen by the same running instance.
 */
@Component
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class SimpleTransactionManager implements TransactionManager {

    // I made this for a demonstration purpose only.
    // It protects against duplicate deliveries within a single running process.
    // In a real-world scenario, I would use a database or a message broker for this.
    Set<String> processedKeys = ConcurrentHashMap.newKeySet();

    /**
     * Records the provided key and reports whether it was already processed by this instance.
     *
     * @param eventKey broker message key used to identify a settlement work unit.
     * @return {@code true} if the key was already present, otherwise {@code false}.
     */
    @Override
    public boolean isDuplicate(String eventKey) {
        return !processedKeys.add(eventKey);
    }

}
