package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.TransactionStore;
import java.net.URI;
import java.util.List;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionResource.class);

    private final TransactionStore store;

    public TransactionResource(final TransactionStore store) {
        this.store = store;
    }

    @GET
    public List<Transaction> fetch() {
        LOG.info("Fetching all transactions");
        return store.getAll();
    }

    @Path("/{index}")
    public Optional<Transaction> fetch(@PathParam("index") int identifier) {
        LOG.info("Fetching transaction (id: {})", identifier);
        return store.get(identifier);
    }

    @POST
    public Response add(final @Valid Transaction newTx) {
        long availableFunds = store.find(newTx.getFrom())
                .stream()
                .mapToLong(tx -> tx.getFrom().equals(newTx.getFrom()) ? -tx.getAmount() : tx.getAmount())
                .sum();
        if (availableFunds < newTx.getAmount()) {
            LOG.info("Unable to add new transaction: insufficient funds for account (id: {})", newTx.getFrom());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Insufficient funds")
                    .build();
        }
        int identifier = store.add(newTx);
        LOG.info("Added new transaction (id: {})", identifier);
        URI location = URI.create(String.format("transaction/%d", identifier));
        return Response.created(location).build();
    }
}
