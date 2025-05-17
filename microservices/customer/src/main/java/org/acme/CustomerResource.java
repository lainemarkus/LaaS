package org.acme;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("Customer")
public class CustomerResource {

    @Inject
    io.vertx.mutiny.mysqlclient.MySQLPool client;
    
    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") 
    boolean schemaCreate ;

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }
    
    private void initdb() {
        // In a production environment this configuration SHOULD NOT be used
        client.query("DROP TABLE IF EXISTS Customers").execute()
        .flatMap(r -> client.query("CREATE TABLE Customers (id SERIAL PRIMARY KEY, name TEXT NOT NULL, FiscalNumber VARCHAR(20) NOT NULL, address TEXT NOT NULL, postalCode VARCHAR(10) NOT NULL)").execute())
        .flatMap(r -> client.query("INSERT INTO Customers (name,FiscalNumber,address, postalcode) VALUES ('client1','245575600','Lisbon', '2725-605')").execute())
        .flatMap(r -> client.query("INSERT INTO Customers (name,FiscalNumber,address, postalcode) VALUES ('client2','987654123','Setúbal', '2725-605')").execute())
        .flatMap(r -> client.query("INSERT INTO Customers (name,FiscalNumber,address, postalcode) VALUES ('client3','123987543','Porto', '2725-605')").execute())
        .flatMap(r -> client.query("INSERT INTO Customers (name,FiscalNumber,address, postalcode) VALUES ('client4','987123876','Faro', '2725-605')").execute())
        .await().indefinitely();
    }
    
    @GET
    public Multi<Customer> get() {
        return Customer.findAll(client);
    }
    
    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return Customer.findById(client, id)
                .onItem().transform(customer -> customer != null ? Response.ok(customer) : Response.status(Response.Status.NOT_FOUND)) 
                .onItem().transform(ResponseBuilder::build); 
    }
     
    @POST
    public Uni<Response> create(Customer customer) {
        return customer.save(client , customer.name , customer.FiscalNumber.getValue() , customer.location.getAddress(), customer.location.getPostalCode().getValue())
                .onItem().transform(id -> URI.create("/customer/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }
    
    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Customer.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
    
}
