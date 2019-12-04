package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.TransactionStore;
import com.github.korthout.db.TransactionStore.Direction;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    public Response add(final @Valid Transaction tx) {
        Map<Direction, List<Transaction>> txsFrom = store.find(tx.getFrom());
        Integer totalReceived = txsFrom.get(Direction.TO)
                .stream()
                .map(Transaction::getAmount)
                .reduce(Integer::sum)
                .orElse(0);
        Integer totalSpent = txsFrom.get(Direction.FROM)
                .stream()
                .map(Transaction::getAmount)
                .reduce(Integer::sum)
                .orElse(0);
        if (totalReceived - totalSpent < tx.getAmount()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Insufficient funds").build();
        }
        int index = store.add(tx);
        URI location = URI.create(String.format("transaction/%d", index));
        return Response.created(location).build();
    }
}
