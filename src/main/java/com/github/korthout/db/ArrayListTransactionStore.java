package com.github.korthout.db;

import com.github.korthout.api.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * In memory transaction data store
 */
public class ArrayListTransactionStore implements TransactionStore {

    private final List<Transaction> internal;

    public ArrayListTransactionStore() {
        this.internal = new ArrayList<>();
    }

    @Override
    public synchronized int add(final Transaction tx) {
        internal.add(tx);
        return internal.size();
    }

    /**
     * Clear the store, for easy testing.
     * Warning! Not thread-safe!
     */
    public void clear() {
        internal.clear();
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
    public List<Transaction> getAll() {
        return new ArrayList<>(internal);
    }

    @Override
    public List<Transaction> find(final UUID account) {
        return this.getAll().stream()
                .filter(tx -> account.equals(tx.getFrom()) || account.equals(tx.getTo()))
                .collect(Collectors.toList());
    }

}
