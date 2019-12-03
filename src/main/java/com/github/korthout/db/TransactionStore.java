package com.github.korthout.db;

import com.github.korthout.api.Transaction;
import java.util.ArrayList;
import java.util.List;

/**
 * In memory transaction data store
 */
public class TransactionStore {

    private final List<Transaction> internal;

    public TransactionStore() {
        this.internal = new ArrayList<>();
    }

    /**
     * Stores a transaction
     * @param tx Transaction to store
     * @return storage index
     */
    public synchronized int add(Transaction tx) {
        internal.add(tx);
        return internal.size();
    }

    /**
     * Clear the store, for easy testing
     */
    public void clear() {
        internal.clear();
    }

    /**
     * Retrieve all transactions
     * @return a list of all known transactions at this moment
     */
    public List<Transaction> getAll() {
        return new ArrayList<>(internal);
    }
}
