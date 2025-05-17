package org.acme;

import org.acme.model.Topic;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("Purchase")
public class KafkaProvisioningResource {

    @Inject
    io.vertx.mutiny.mysqlclient.MySQLPool client;

    @Inject
    @ConfigProperty(name = "myapp.schema.create", defaultValue = "true")
    boolean schemaCreate;

    @ConfigProperty(name = "kafka.bootstrap.servers")
    String kafka_servers;

    void config(@Observes StartupEvent ev) {
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
        client.query("DROP TABLE IF EXISTS Purchases").execute()
                .flatMap(r -> client.query(
                "CREATE TABLE Purchases ("
                + "id SERIAL PRIMARY KEY, "
                + "DateTime DATETIME, "
                + "Price FLOAT, "
                + "product_name TEXT NOT NULL, "
                + "supplier_id BIGINT UNSIGNED NOT NULL, "
                + "supplier_name TEXT NOT NULL, "
                + "shop_id BIGINT UNSIGNED NOT NULL, "
                + "loyaltycard_id BIGINT UNSIGNED NOT NULL, "
                + "FOREIGN KEY (shop_id) REFERENCES Shops(id), "
                + "FOREIGN KEY (loyaltycard_id) REFERENCES LoyaltyCards(id)"
                + ")"
        ).execute())
                .flatMap(r -> client.query(
                "INSERT INTO Purchases (DateTime, Price, product_name, supplier_id, supplier_name, shop_id, loyaltycard_id) "
                + "VALUES ('2038-01-19 03:14:07', 12.34, 'Shampoo', 1, 'FabricaChinaShampoo', 1, 1)"
        ).execute())
                .await().indefinitely();
    }

    @POST
    @Path("Consume")
    public String ProvisioningConsumer(Topic topic) {
        Thread worker = new DynamicTopicConsumer(topic.TopicName, kafka_servers, client);
        worker.start();
        return "New worker started";
    }

    @GET
    public Multi<Purchase> get() {
        return Purchase.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return Purchase.findById(client, id)
                .onItem().transform(purchase -> purchase != null ? Response.ok(purchase) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(Response.ResponseBuilder::build);
    }

}
