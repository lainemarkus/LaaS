package org.acme;

import java.time.LocalDateTime;

import org.acme.common.LoyaltyCardID;
import org.acme.common.ShopID;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Purchase {

    public Long id;
    public LocalDateTime timestamp;
    public Float price;
    public ProductType product;
    public ShopID shopId;
    public LoyaltyCardID loyaltyCardId;

    public Purchase() {
    }

    public Purchase(Long id, LocalDateTime timestamp, Float price, ProductType product, ShopID shopId, LoyaltyCardID loyaltyCardId) {
        this.id = id;
        this.timestamp = timestamp;
        this.price = price;
        this.product = product;
        this.shopId = shopId;
        this.loyaltyCardId = loyaltyCardId;
    }

    @Override
    public String toString() {
        return "{id=" + id
                + ", timestamp=" + timestamp
                + ", price=" + price
                + ", product=" + product
                + ", shopId=" + shopId
                + ", loyaltyCardId=" + loyaltyCardId
                + "}";
    }

    private static Purchase from(Row row) {
        SupplierType supplier = new SupplierType(
            new SupplierID(row.getLong("supplier_id")),
            row.getString("supplier_name")
        );

        return new Purchase(
                row.getLong("id"),
                row.getLocalDateTime("DateTime"),
                row.getFloat("Price"),
                new ProductType(row.getString("product_name"), supplier),
                new ShopID(row.getLong("shop_id")),
                new LoyaltyCardID(row.getLong("loyaltycard_id"))
        );
    }

    public static Multi<Purchase> findAll(MySQLPool client) {
        return client.query("SELECT id, DateTime, Price, Product, shop_id, loyaltycard_id FROM Purchases ORDER BY id ASC")
                .execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Purchase::from);
    }

    public static Uni<Purchase> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, DateTime, Price, Product, shop_id, loyaltycard_id FROM Purchases WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    // Add save, update, delete methods similarly adapting to new types and DB schema
}
