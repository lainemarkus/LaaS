package org.acme;

import java.net.URI;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

@Path("Shop")
public class ShopResource {

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
        client.query("DROP TABLE IF EXISTS Shops").execute()
                .flatMap(r -> client.query("CREATE TABLE Shops (id SERIAL PRIMARY KEY, name TEXT NOT NULL, address TEXT NOT NULL, postalCode VARCHAR(10) NOT NULL)").execute())
                .flatMap(r -> client.query("INSERT INTO Shops (name, address, postalCode) VALUES ('ArcoCegoLisbon','Lisboa', '2725-242')").execute())
                .flatMap(r -> client.query("INSERT INTO Shops (name, address, postalCode) VALUES ('PracadeBocage','Setubal', '2725-242')").execute())
                .flatMap(r -> client.query("INSERT INTO Shops (name, address, postalCode) VALUES ('PracadaBoavista','Porto', '2725-242')").execute())
                .flatMap(r -> client.query("INSERT INTO Shops (name, address, postalCode) VALUES ('PracaDomFranciscoGomes','Faro', '2725-242')").execute())
                .await().indefinitely();
    }

    @GET
    public Multi<Shop> get() {
        return Shop.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return Shop.findById(client, id)
                .onItem().transform(shop -> shop != null ? Response.ok(shop) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Shop shop) {
        return shop.save(
                client,
                shop.name,
                shop.location.getAddress(),
                shop.location.getPostalCode().getValue()
        )
                .onItem().transform(created -> URI.create("/shop/" + created))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Shop.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    //NOT ASKED
    // @PUT
    // @Path("/{id}/{name}/{address}/{postalCode}")
    // public Uni<Response> update(@PathParam("id") Long id,
    //         @PathParam("name") String name,
    //         @PathParam("address") String address,
    //         @PathParam("postalCode") String postalCode) {
    //     return Shop.update(client, id, name, address, postalCode)
    //             .onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
    //             .onItem().transform(status -> Response.status(status).build());
    // }

}
