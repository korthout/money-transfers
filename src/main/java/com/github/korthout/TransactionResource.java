package com.github.korthout;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    public List<Transaction> fetch(@QueryParam("account") @Nullable final UUID account) {
        if (account != null) {
            LOG.info("Fetching all transactions for account (id: {})", account);
            return store.find(account);
        }
        LOG.info("Fetching all transactions");
        return store.getAll();
    }

    @GET
    @Path("/{index}")
    public Optional<Transaction> fetch(@PathParam("index") final int identifier) {
        LOG.info("Fetching transaction (id: {})", identifier);
        return store.get(identifier);
    }

    @POST
    public Response add(@Valid @NotNull final Transaction newTx) {
        int identifier = store.add(newTx);
        LOG.info("Added new transaction (id: {})", identifier);
        URI location = URI.create(String.format("transactions/%d", identifier));
        return Response.created(location).build();
    }
}
