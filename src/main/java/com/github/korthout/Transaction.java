package com.github.korthout;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Generated;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Transaction {

    @Min(0) private final long amount;
    @NotNull private final UUID from;
    @NotNull private final UUID to;

    @JsonCreator
    public Transaction(
            @JsonProperty("amount") final long amount,
            @JsonProperty("from") final UUID from,
            @JsonProperty("to") final UUID to
    ) {
        this.amount = amount;
        this.from = from;
        this.to = to;
    }

    @JsonProperty("amount")
    public long getAmount() {
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

    @Generated("IntelliJ IDEA 2019.3")
    @Override
    public String toString() {
        return "Transaction{" +
                "amount=" + amount +
                ", from=" + from +
                ", to=" + to +
                '}';
    }
}
