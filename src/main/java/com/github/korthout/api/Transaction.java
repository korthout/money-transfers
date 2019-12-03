package com.github.korthout.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Generated;

public class Transaction {

    private final int amount;
    private final UUID from;
    private final UUID to;

    @JsonCreator
    public Transaction(
            @JsonProperty("amount") final int amount,
            @JsonProperty("from") final UUID from,
            @JsonProperty("to") final UUID to
    ) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    @JsonProperty("amount")
    public int getAmount() {
        return amount;
    }

    @JsonProperty("from")
    public UUID getFrom() {
        return from;
    }

    @JsonProperty("to")
    public UUID getTo() {
        return to;
    }

    @Generated("IntelliJ IDEA 2019.3")
    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Transaction that = (Transaction) o;
        return amount == that.amount &&
                from.equals(that.from) &&
                to.equals(that.to);
    }

    @Generated("IntelliJ IDEA 2019.3")
    @Override
    public int hashCode() {
        return Objects.hash(amount, from, to);
    }
}
