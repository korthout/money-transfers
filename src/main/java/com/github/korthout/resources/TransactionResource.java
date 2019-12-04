package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.TransactionStore;
import com.github.korthout.db.TransactionStore.Direction;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("transaction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private final TransactionStore store;

    public TransactionResource(final TransactionStore store) {
        this.store = store;
    }

    @GET
    public List<Transaction> fetch() {
        return store.getAll();
    }

    @Path("/{index}")
    public Optional<Transaction> fetch(@PathParam("index") int identifier) {
        return store.get(identifier);
    }

    @POST
    public Response add(final @Valid Transaction newTx) {

         //     * Find all transactions belonging to a specific account
         //     * @param account The account to search txs for
         //     * @return mapping of transactions grouped by direction from or to that account
        Map<Direction, List<Transaction>> initialMap = new EnumMap<>(Direction.class);
        initialMap.put(Direction.FROM, new ArrayList<>());
        initialMap.put(Direction.TO, new ArrayList<>());
        // todo refactor this to Map<Direction, Integer> as mapping of direction => sum
        Map<Direction, List<Transaction>> txsFrom = store.find(newTx.getFrom()).stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getFrom().equals(newTx.getFrom()) ? Direction.FROM : Direction.TO,
                        () -> initialMap,
                        Collectors.toList()
                ));
        Long totalReceived = txsFrom.get(Direction.TO)
                .stream()
                .map(Transaction::getAmount)
                .reduce(Long::sum)
                .orElse(0L);
        Long totalSpent = txsFrom.get(Direction.FROM)
                .stream()
                .map(Transaction::getAmount)
                .reduce(Long::sum)
                .orElse(0L);
        if (totalReceived - totalSpent < newTx.getAmount()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Insufficient funds").build();
        }
        int index = store.add(newTx);
        URI location = URI.create(String.format("transaction/%d", index));
        return Response.created(location).build();
    }
}
