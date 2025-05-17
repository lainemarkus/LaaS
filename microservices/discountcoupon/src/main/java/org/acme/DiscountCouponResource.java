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

@Path("discountcoupon")
public class DiscountCouponResource {

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
        client.query("DROP TABLE IF EXISTS DiscountCoupons").execute()
            .flatMap(r -> client.query(
                "CREATE TABLE DiscountCoupons (" +
                "id SERIAL PRIMARY KEY, " +
                "loyaltycard_id BIGINT UNSIGNED NOT NULL, " +
                "shop_id BIGINT UNSIGNED NOT NULL, " +
                "discount_code VARCHAR(50), " +
                "discount_amount DOUBLE, " +
                "expiry_date DATETIME, " +
                "FOREIGN KEY (loyaltycard_id) REFERENCES LoyaltyCards(id), " +
                "FOREIGN KEY (shop_id) REFERENCES Shops(id))"
            ).execute())
            .flatMap(r -> client.query(
                "INSERT INTO DiscountCoupons (loyaltycard_id, shop_id, discount_code, discount_amount, expiry_date) " +
                "VALUES (1, 1, 'WELCOME10', 10.0, '2038-01-19 03:14:07')"
            ).execute())
            .await().indefinitely();
    }

    @GET
    public Multi<DiscountCoupon> get() {
        return DiscountCoupon.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return DiscountCoupon.findById(client, id)
                .onItem().transform(coupon -> coupon != null ? Response.ok(coupon) : Response.status(Response.Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(DiscountCoupon coupon) {
        return coupon.save(client)
                .onItem().transform(created -> URI.create("/discountcoupon/" + coupon.id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return DiscountCoupon.delete(client, id)
                .onItem().transform(deleted -> deleted ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    // @PUT
    // @Path("{id}")
    // public Uni<Response> update(@PathParam("id") Long id, DiscountCoupon coupon) {
    //     return DiscountCoupon.update(
    //                 client,
    //                 id,
    //                 coupon.idLoyaltyCard,
    //                 coupon.idShop,
    //                 coupon.discount,
    //                 coupon.expiryDate
    //             )
    //             .onItem().transform(updated -> updated ? Response.Status.NO_CONTENT : Response.Status.NOT_FOUND)
    //             .onItem().transform(status -> Response.status(status).build());
    // }
}