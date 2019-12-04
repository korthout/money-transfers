package com.github.korthout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionStore {
    /**
     * Stores a transaction
     * @param tx Transaction to store
     * @return a unique identifier for the transaction
     */
    int add(Transaction tx);

    /**
     * Retrieves a specific transaction
     * @param identifier unique identifier for the transaction
     * @return optional of the found transaction or empty optional
     */
    Optional<Transaction> get(int identifier);

    /**
     * Retrieves all transactions
     * @return a list of all stored transactions
     */
    List<Transaction> getAll();

    /**
     * Finds all transaction from or to a specific account
     * @param account the account to find transactions for
     * @return a list of all stored transaction of this account
     */
    List<Transaction> find(UUID account);

}
