package com.github.korthout;

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

    // use the class and not the interface directly for testing purposes
    private static final VavrListTransactionStore TX_STORE = new VavrListTransactionStore();

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new TransactionResource(TX_STORE))
            .build();

    private static Invocation.Builder request(String... paths) {
        return RESOURCES.target("transactions")
                .path(String.join("/", paths))
                .request();
    }

    private static Invocation.Builder queriedRequest(String name, Object value) {
        return RESOURCES.target("transactions")
                .queryParam(name, value)
                .request();
    }

    @After
    public void tearDown(){
        TX_STORE.clear();
    }

    @Test
    public void noTransactionsExistsAtStartup() {
        assertThat(request().get(new GenericType<List<Transaction>>(){}))
                .isEmpty();
    }

    @Test
    public void storedTransactionsCanBeRetrieved() {
        Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        TX_STORE.add(tx);
        assertThat(request().get(new GenericType<List<Transaction>>(){}))
                .contains(tx);
    }

    @Test
    public void postingTransactionsResultsIn201CreatedWithLocationHeader() {
        Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        assertThat(request().post(Entity.json(tx)))
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
                    assertThat(response.getLocation().toString()).endsWith("/transactions/1");
                });
    }

    @Test
    public void transactionsHaveTheirOwnUniqueResourceLocation() {
        Transaction tx1 = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        Transaction tx2 = new Transaction(35, UUID.randomUUID(), UUID.randomUUID());
        assertThat(request().post(Entity.json(tx1)).getLocation().toString()).endsWith("/transactions/1");
        assertThat(request().post(Entity.json(tx2)).getLocation().toString()).endsWith("/transactions/2");
    }

    @Test
    public void transactionsCanBeRetrievedUsingTheirUniqueLocation() {
        assertThat(request("1").get().getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        request().post(Entity.json(tx));
        assertThat(request("1").get()).satisfies(response -> {
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            assertThat(response.readEntity(Transaction.class)).isEqualTo(tx);
        });
    }

    @Test
    public void transactionsCanBeFetchedByAccount() {
        UUID specificAccount = UUID.randomUUID();
        Transaction tx1 = new Transaction(100, specificAccount, UUID.randomUUID());
        Transaction tx2 = new Transaction(100, UUID.randomUUID(), specificAccount);
        Transaction tx3 = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        TX_STORE.add(tx1);
        TX_STORE.add(tx2);
        TX_STORE.add(tx3);
        assertThat(queriedRequest("account", specificAccount).get())
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
                    assertThat(response.readEntity(new GenericType<List<Transaction>>(){}))
                            .containsExactlyInAnyOrder(tx1, tx2);
                });
    }

    @Test
    public void canCorrectlyStoreThousandsOfConcurrentTransactions() {
        // concurrently send txs with amounts ranging from 1 to 5000
        IntStream.rangeClosed(1, 5000)
                .parallel()
                .mapToObj(i -> new Transaction(i, UUID.randomUUID(), UUID.randomUUID()))
                .map(Entity::json)
                .forEach(json -> request().post(json));
        // verify that all amounts had been stored exactly once
        Map<Long, List<Transaction>> allTxsGroupedByAmount = TX_STORE.getAll().stream()
                .collect(Collectors.groupingBy(Transaction::getAmount));
        assertThat(allTxsGroupedByAmount.keySet()).hasSize(5000);
        assertThat(allTxsGroupedByAmount.values()).allSatisfy(
                txsWithSpecificAmount -> assertThat(txsWithSpecificAmount).hasSize(1)
        );
    }

}