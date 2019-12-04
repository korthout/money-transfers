package com.github.korthout;

import com.github.korthout.db.ArrayListTransactionStore;
import com.github.korthout.resources.TransactionResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MoneyTransfersApplication extends Application<MoneyTransfersConfiguration> {

    public static void main(final String[] args) throws Exception {
        new MoneyTransfersApplication().run(args);
    }

    @Override
    public String getName() {
        return "MoneyTransfers";
    }

    @Override
    public void initialize(final Bootstrap<MoneyTransfersConfiguration> bootstrap) {

    }

    @Override
    public void run(final MoneyTransfersConfiguration configuration,
                    final Environment environment) {
        environment.jersey().register(new TransactionResource(new ArrayListTransactionStore()));
    }

}
