package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Shop {

    public ShopID id;
    public LocationType location;
    public String name;

    public Shop() {
    }

    public Shop(String name) {
        this.name = name;
    }

    public Shop(ShopID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Shop(ShopID iD, String name_R, LocationType location_R) {
        id = iD;
        location = location_R;
        name = name_R;
    }

    @Override
    public String toString() {
        return "{ id:" + id.getValue() + ", location:" + location.getAddress() + ", name:" + name + "}\n";
    }

    private static Shop from(Row row) {
        return new Shop(new ShopID(row.getLong("id")), row.getString("name"), new LocationType(row.getString("address"), new PostalCodeType(row.getString("postalCode"))));
    }

    public static Multi<Shop> findAll(MySQLPool client) {
        return client.query("SELECT id, name, address, postalCode FROM Shops ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Shop::from);
    }

    public static Uni<Shop> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, name, address, postalCode FROM Shops WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client, String name, String address, String postalCode) {
        return client.preparedQuery("INSERT INTO Shops(name, address, postalCode) VALUES (?, ?, ?)")
                .execute(Tuple.of(name, address, postalCode))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id) {
        return client.preparedQuery("DELETE FROM Shops WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id, String name, String address, String postalCode) {
        return client.preparedQuery("UPDATE Shops SET name = ?, address = ?, postalCode = ? WHERE id = ?")
                .execute(Tuple.of(name, address, postalCode, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }
}
