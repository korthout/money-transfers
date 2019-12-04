# MoneyTransfers: a homework assignment

This application offers a simple RESTful API for money transfers between accounts.
Note: data is not persisted and will be lost after application shutdown.

How to start the MoneyTransfers application
---

1. Run `mvn clean install` to build and test the application
2. Start the application with `java -jar target/money-transfers-1.0-SNAPSHOT.jar server config.yml`.
3. You should be available to view transactions at http://localhost:8080/transactions.

Design Considerations
---

### Storage
Transactions between accounts need to be stored. This can be done in multiple ways, e.g.:

1. adding the amount to an account and subtracting it from another account and storing only the results
2. split-up as a debit entry to an account and a credit entry to another account
3. as an amount going from an account to another account

Each of these has pros, but also cons:

1. loss of history: it is not possible to see what happened, or how
2. loss of traceability: it is not possible to see which account sent money to a specific account,
   or which received money from a specific account 
3. loss of efficiency: it takes time to calculate the current available funds of an account

Combinations are also possible, but may lead to double bookkeeping and no single source of truth.
For ease, I've chosen option 3 and taking the hit in efficiency.

### Starting situation
In order to send money, we need to have money to sent. If available money is only stored as 
transactions between accounts than we either need some initial transaction(s) sending money from
no account to some managed account. This managed account can then be used to represent incoming 
money transfers from outside of the application (e.g. cash, SEPA, cryptocurrency, etc.). 

Another option is to allow accounts to have negative available funds. Again, I've chosen to just
keep it simple. If we would need to represent transactions crossing the application's boundaries
then we can simply create transactions for a managed account again, but we don't need initial
transactions to represent some available amount of money to sent around in the system.

### Concurrency
To guarantee correct behaviour while multiple users are transferring money, we need to consider 
concurrency. I've chosen Dropwizard to quickly develop this RESTful API. Dropwizard uses Jersey to 
handle HTTP requests. Each HTTP request is handled transparently by a thread from a threadpool.
This allows multiple requests to be received simultaneously. 

Storing the data from these requests must be handled thread-safe. I've used an immutable list to
represent the history of received transactions at a particular time. Storing a new transaction is
as simple as appending it to the list, without modifying the original. Reading this list at any time
is thread-safe, because in Java it is simply a reference to an object in the heap, and that object 
will never change anymore. Writing needs to be done more carefully. No 2 write actions can be done 
simultaneously, because of a potential race condition.

The rest of the application is stateless, and thus also thread-safe.

