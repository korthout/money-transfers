package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.TransactionStore;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResourceTest {

    private static final TransactionStore store = new TransactionStore();

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TransactionResource(store))
            .build();

    @After
    public void tearDown(){
        store.clear();
    }

    @Test
    public void noTransactionsExistsAtStartup() {
        assertThat(request().get(new GenericType<List<Transaction>>(){}))
                .isEmpty();
    }

    @Test
    public void postingTransactionsResultsIn201Created() {
        final Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        assertThat(request().post(Entity.json(tx)))
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
                    assertThat(response.getLocation().toString()).endsWith("/transaction/1");
                });
    }

    @Test
    public void storedTransactionsCanBeRetrieved() {
        final Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        request().post(Entity.json(tx));
        assertThat(request().get(new GenericType<List<Transaction>>(){}))
                .contains(tx);
    }

    @Test
    public void canEasilyStoreThousandsOfTransactions() {
        IntStream.range(0, 5000)
                .parallel()
                .mapToObj(i -> new Transaction(i, UUID.randomUUID(), UUID.randomUUID()))
                .map(Entity::json)
                .forEach(json -> request().post(json));
        Map<Integer, List<Transaction>> allTxsGroupedByAmount = request()
                .get(new GenericType<List<Transaction>>() {})
                .stream()
                .collect(Collectors.groupingBy(Transaction::getAmount));
        // verify that all amounts had been stored exactly once
        assertThat(allTxsGroupedByAmount.keySet()).hasSize(5000);
        assertThat(allTxsGroupedByAmount.values()).allSatisfy(
                txsWithSpecificAmount -> assertThat(txsWithSpecificAmount).hasSize(1)
        );
    }

    private Invocation.Builder request() {
        return resources.target("transaction").request();
    }

}