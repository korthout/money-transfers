package com.github.korthout.db;

import com.github.korthout.api.Transaction;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public void clear() {
        internal.clear();
    }

    @Override
    public Optional<Transaction> get(final int identifier) {
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
    public Map<Direction, List<Transaction>> find(final UUID account) {
        // todo push most of this implementation down to the resource class
        Map<Direction, List<Transaction>> initialMap = new EnumMap<>(Direction.class);
        initialMap.put(Direction.FROM, new ArrayList<>());
        initialMap.put(Direction.TO, new ArrayList<>());
        return this.getAll()
                .stream()
                .filter(tx -> account.equals(tx.getFrom()) || account.equals(tx.getTo()))
                .collect(Collectors.groupingBy(
                        tx -> tx.getFrom().equals(account) ? Direction.FROM : Direction.TO,
                        () -> initialMap,
                        Collectors.toList()
                ));
    }

}
