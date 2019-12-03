package com.github.korthout.resources;

import com.github.korthout.api.Transaction;
import com.github.korthout.db.TransactionStore;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    @POST
    public Response add(final @Valid Transaction tx) {
        int id = store.add(tx);
        URI location = URI.create(String.format("transaction/%d", id));
        return Response.created(location).build();
    }
}
