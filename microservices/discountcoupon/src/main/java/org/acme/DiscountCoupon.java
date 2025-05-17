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

public class DiscountCoupon {

    public Long id;
    public LoyaltyCardID idLoyaltyCard;
    public ShopID idShop;
    public DiscountType discount;
    public LocalDateTime expiryDate;

    public DiscountCoupon() {
    }

    public DiscountCoupon(Long id, LoyaltyCardID idLoyaltyCard, ShopID idShop, DiscountType discount, LocalDateTime expiryDate) {
        this.id = id;
        this.idLoyaltyCard = idLoyaltyCard;
        this.idShop = idShop;
        this.discount = discount;
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return "{id:" + id
                + ", idLoyaltyCard:" + idLoyaltyCard
                + ", idShop:" + idShop
                + ", discount:" + discount
                + ", expiryDate:" + expiryDate + "}";
    }

    private static DiscountCoupon from(Row row) {
        return new DiscountCoupon(
                row.getLong("id"),
                new LoyaltyCardID(row.getLong("loyaltycard_id")),
                new ShopID(row.getLong("shop_id")),
                new DiscountType(
                        row.getString("discount_code"),
                        row.getDouble("discount_amount") 
                ),
                row.getLocalDateTime("expiry_date")
        );
    }

    public static Multi<DiscountCoupon> findAll(MySQLPool client) {
        return client.query("SELECT id, loyaltycard_id, shop_id, discount, expiry_date FROM DiscountCoupons ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(DiscountCoupon::from);
    }

    public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, loyaltycard_id, shop_id, discount, expiry_date FROM DiscountCoupons WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client) {
        return client.preparedQuery("INSERT INTO DiscountCoupons (loyaltycard_id, shop_id, discount, expiry_date) VALUES (?, ?, ?, ?)")
                .execute(Tuple.of(idLoyaltyCard.getValue(), idShop.getValue(), discount.getValue(), expiryDate))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id) {
        return client.preparedQuery("DELETE FROM DiscountCoupons WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id, LoyaltyCardID idLoyaltyCard, ShopID idShop, DiscountType discount, LocalDateTime expiryDate) {
        return client.preparedQuery("UPDATE DiscountCoupons SET loyaltycard_id = ?, shop_id = ?, discount = ?, expiry_date = ? WHERE id = ?")
                .execute(Tuple.of(idLoyaltyCard.getValue(), idShop.getValue(), discount.getValue(), expiryDate, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
