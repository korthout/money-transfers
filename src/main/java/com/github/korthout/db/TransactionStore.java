package com.github.korthout.db;

import com.github.korthout.api.Transaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface TransactionStore {
    /**
     * Stores a transaction (write is thread safe)
     * @param tx Transaction to store
     * @return a unique identifier for the transaction
     */
    int add(Transaction tx);

    /**
     * Clear the store, for easy testing
     */
    void clear();

    Optional<Transaction> get(int identifier);

    /**
     * Retrieve all transactions
     * @return a list of all known transactions at this moment
     */
    List<Transaction> getAll();

    /**
     * Find all transactions belonging to a specific account
     * @param account The account to search txs for
     * @return mapping of transactions grouped by direction from or to that account
     */
    Map<Direction, List<Transaction>> find(UUID account);

    enum Direction {
        FROM, TO
    }
}
