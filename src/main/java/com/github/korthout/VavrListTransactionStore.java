package com.github.korthout;

import io.vavr.collection.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe in-memory transaction store using vavr.
 */
public class VavrListTransactionStore implements TransactionStore {

    private final Lock lock;

    /**
     * Mutable in-memory representation of the store
     * using an immutable thread-safe functional List
     */
    private List<Transaction> internal;

    public VavrListTransactionStore() {
        this.internal = List.empty();
        this.lock = new ReentrantLock();
    }

    /**
     * Clear the store, for easy testing.
     * Warning! Not thread-safe!
     */
    public void clear() {
        this.internal = List.empty();
    }

    @Override
    public int add(final Transaction tx) {
        lock.lock();
        try {
            internal = internal.append(tx);
            return internal.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Transaction> get(final int identifier) {
        // identifier is the storage size directly after the tx is stored
        final int index = identifier - 1;
        if (index < 0 || index >= internal.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(internal.get(index));
    }

    @Override
    public java.util.List<Transaction> getAll() {
        return internal.asJava();
    }

    @Override
    public java.util.List<Transaction> find(final UUID account) {
        return internal.filter(tx -> account.equals(tx.getFrom()) || account.equals(tx.getTo()))
                .asJava();
    }
}
