package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.VavrListTransactionStore;
import io.dropwizard.testing.junit.ResourceTestRule;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResourceTest {

    // use the class and not the interface directly for testing purposes
    private static final VavrListTransactionStore TX_STORE = new VavrListTransactionStore();

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new TransactionResource(TX_STORE))
            .build();

    private static Invocation.Builder request() {
        return RESOURCES.target("transactions").request();
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
    public void transactionsCanNotBeMadeWhenSenderHasInsufficientFunds() {
        // random account does not yet have any transactions
        Transaction tx = new Transaction(100, UUID.randomUUID(), UUID.randomUUID());
        assertThat(request().post(Entity.json(tx)))
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
                    assertThat(response.readEntity(String.class)).isEqualTo("Insufficient funds");
                });
    }

    @Test
    public void postingTransactionsResultsIn201CreatedWithLocationHeader() {
        // provide the sender with some initial money
        UUID sender = UUID.randomUUID();
        TX_STORE.add(new Transaction(100, UUID.randomUUID(), sender));
        // now we can send money from sender to random account
        Transaction tx = new Transaction(100, sender, UUID.randomUUID());
        assertThat(request().post(Entity.json(tx)))
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
                    assertThat(response.getLocation().toString()).endsWith("/transaction/2");
                });
    }

    @Test
    public void spendingMoneyReducesAvailableFunds() {
        UUID sender = UUID.randomUUID();
        TX_STORE.add(new Transaction(100, UUID.randomUUID(), sender));
        TX_STORE.add(new Transaction(50, sender, UUID.randomUUID()));
        Transaction tooLargeTx = new Transaction(51, sender, UUID.randomUUID());
        assertThat(request().post(Entity.json(tooLargeTx)))
                .satisfies(response -> {
                    assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
                    assertThat(response.readEntity(String.class)).isEqualTo("Insufficient funds");
                });
        Transaction exactAmountAvailableTx = new Transaction(50, sender, UUID.randomUUID());
        assertThat(request().post(Entity.json(exactAmountAvailableTx)).getStatus())
                .isEqualTo(HttpStatus.CREATED_201);
    }

    @Ignore
    @Test
    public void transactionsCanBeFetchedUsingTheLocationHeader() {
        UUID sender = UUID.randomUUID();
        TX_STORE.add(new Transaction(100, UUID.randomUUID(), sender));
        Transaction tx = new Transaction(100, sender, UUID.randomUUID());
        Response response = request().post(Entity.json(tx));
        URI location = response.getLocation();
        // todo this doesn't work yet
        assertThat(RESOURCES.client().target(location).request().get().readEntity(Transaction.class))
                .isEqualTo(tx);
    }

    @Test
    public void canCorrectlyStoreThousandsOfConcurrentTransactions() {
        // make sure sender has enough available funds
        UUID sender = UUID.randomUUID();
        TX_STORE.add(new Transaction(10000000, UUID.randomUUID(), sender));

        // concurrently send txs with amounts ranging from 1 to 5000
        IntStream.range(1, 2000)
                .parallel()
                .mapToObj(i -> new Transaction(i, sender, UUID.randomUUID()))
                .map(Entity::json)
                .forEach(json -> request().post(json));
        // verify that all amounts had been stored exactly once
        Map<Long, List<Transaction>> allTxsGroupedByAmount = TX_STORE.getAll().stream()
                .collect(Collectors.groupingBy(Transaction::getAmount));
        assertThat(allTxsGroupedByAmount.keySet()).hasSize(2000);
        assertThat(allTxsGroupedByAmount.values()).allSatisfy(
                txsWithSpecificAmount -> assertThat(txsWithSpecificAmount).hasSize(1)
        );
    }

}