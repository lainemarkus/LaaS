package org.acme;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("Loyaltycard")
public class LoyaltycardResource {

    @Inject
    io.vertx.mutiny.mysqlclient.MySQLPool client;

    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
    boolean schemaCreate;

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        // In a production environment this configuration SHOULD NOT be used
        client.query("DROP TABLE IF EXISTS LoyaltyCards").execute()
                .flatMap(r -> client.query("CREATE TABLE LoyaltyCards (id SERIAL PRIMARY KEY, idCustomer BIGINT UNSIGNED, idShop BIGINT UNSIGNED, CONSTRAINT UC_Loyal UNIQUE (idCustomer,idShop), FOREIGN KEY (idCustomer) REFERENCES Customers(id), FOREIGN KEY (idShop) REFERENCES Shops(id))").execute())
                //.flatMap(r -> client.query(" INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (1,1)").execute())
                //.flatMap(r -> client.query(" INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (2,1)").execute())
                //.flatMap(r -> client.query(" INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (1,3)").execute())
                //.flatMap(r -> client.query(" INSERT INTO LoyaltyCards (idCustomer,idShop) VALUES (4,2)").execute())
                .await().indefinitely();
    }

    @GET
    public Multi<Loyaltycard> get() {
        return Loyaltycard.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return Loyaltycard.findById(client, id)
                .onItem().transform(loyaltycard -> loyaltycard != null ? Response.ok(loyaltycard) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @GET
    @Path("{idCustomer}/{idShop}")
    public Uni<Response> getDual(Long idCustomer, Long idShop) {
        return Loyaltycard.findById2(client, idCustomer, idShop)
                .onItem().transform(loyaltycard -> loyaltycard != null ? Response.ok(loyaltycard) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Loyaltycard loyaltycard) {
        return customerExists(loyaltycard.idCustomer)
                .flatMap(existsCustomer -> {
                    if (Boolean.FALSE.equals(existsCustomer)) {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Customer ID does not exist").build());
                    }
                    return shopExists(loyaltycard.idShop);
                })
                .flatMap(existsShop -> {
                    if (Boolean.FALSE.equals(existsShop)) {
                        return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                                .entity("Shop ID does not exist").build());
                    }
                    return loyaltycard.save(client, loyaltycard.idCustomer, loyaltycard.idShop)
                            .onItem().transform(id -> URI.create("/loyaltycard/" + id))
                            .onItem().transform(uri -> Response.created(uri).build());
                });
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Loyaltycard.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    private Uni<Boolean> customerExists(Long idCustomer) {
        return client.preparedQuery("SELECT 1 FROM Customers WHERE id = ?")
                .execute(Tuple.of(idCustomer))
                .onItem().transform(rows -> rows.rowCount() > 0);
    }

    private Uni<Boolean> shopExists(Long idShop) {
        return client.preparedQuery("SELECT 1 FROM Shops WHERE id = ?")
                .execute(Tuple.of(idShop))
                .onItem().transform(rows -> rows.rowCount() > 0);
    }

}
