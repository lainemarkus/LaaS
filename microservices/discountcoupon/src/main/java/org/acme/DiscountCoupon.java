package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class DiscountCoupon {

    public Long id;
    public Long idCustomer;
    public Long idShop;
    public String code;
    public Double discount;

    public DiscountCoupon() {}

    public DiscountCoupon(Long id, Long idCustomer, Long idShop, String code, Double discount) {
        this.id = id;
        this.idCustomer = idCustomer;
        this.idShop = idShop;
        this.code = code;
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "{id:" + id + ", idCustomer:" + idCustomer + ", idShop:" + idShop + ", code:" + code + ", discount:" + discount + "}\n";
    }

    private static DiscountCoupon from(Row row) {
        return new DiscountCoupon(
            row.getLong("id"),
            row.getLong("idCustomer"),
            row.getLong("idShop"),
            row.getString("code"),
            row.getDouble("discount")
        );
    }

    public static Multi<DiscountCoupon> findAll(MySQLPool client) {
        return client.query("SELECT id, idCustomer, idShop, code, discount FROM DiscountCoupons ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(DiscountCoupon::from);
    }

    public static Uni<DiscountCoupon> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, idCustomer, idShop, code, discount FROM DiscountCoupons WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client) {
        return client.preparedQuery("INSERT INTO DiscountCoupons (idCustomer, idShop, code, discount) VALUES (?, ?, ?, ?)")
                .execute(Tuple.of(idCustomer, idShop, code, discount))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id) {
        return client.preparedQuery("DELETE FROM DiscountCoupons WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id, Long idCustomer, Long idShop, String code, Double discount) {
        return client.preparedQuery("UPDATE DiscountCoupons SET idCustomer = ?, idShop = ?, code = ?, discount = ? WHERE id = ?")
                .execute(Tuple.of(idCustomer, idShop, code, discount, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
