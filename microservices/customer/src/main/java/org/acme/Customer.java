package org.acme;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.mysqlclient.MySQLPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Customer {

    public FiscalNumberType FiscalNumber;
    public CustomerID id;
    public LocationType location;
    public String name;

    public Customer() {
    }

    public Customer(String name) {
        this.name = name;
    }

    public Customer(CustomerID iD, String name_R, LocationType location_R, FiscalNumberType FiscalNumber_R) {
        this.FiscalNumber = FiscalNumber_R;
        this.id = iD;
        this.location = location_R;
        this.name = name_R;
    }

    @Override
    public String toString() {
        return "{FiscalNumber:" + FiscalNumber + ", id:" + id + ", location:" + location + ", name:" + name
                + "}\n";
    }

    private static Customer from(Row row) {
        return new Customer(
                new CustomerID(row.getLong("id")),
                row.getString("name"),
                new LocationType(
                        row.getString("address"),
                        new PostalCodeType(row.getString("postalCode"))
                ),
                new FiscalNumberType(row.getString("FiscalNumber"))
        );
    }

    public static Multi<Customer> findAll(MySQLPool client) {
        return client.query("SELECT id, name, FiscalNumber, address, postalCode FROM Customers ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Customer::from);
    }

    public static Uni<Customer> findById(MySQLPool client, Long id) {
        return client.preparedQuery("SELECT id, name, FiscalNumber, address, postalCode FROM Customers WHERE id = ?")
                .execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Boolean> save(MySQLPool client, String name_R, String fiscalNumber, String address, String postalCode) {
        return client.preparedQuery("INSERT INTO Customers(name, FiscalNumber, address, postalCode) VALUES (?, ?, ?, ?)")
                .execute(Tuple.of(name_R, fiscalNumber, address, postalCode))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(MySQLPool client, Long id_R) {
        return client.preparedQuery("DELETE FROM Customers WHERE id = ?").execute(Tuple.of(id_R))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> update(MySQLPool client, Long id_R, String name_R, String fiscalNumber, String address, String postalCode) {
        return client.preparedQuery("UPDATE Customers SET name = ?, FiscalNumber = ?, address = ?, postalCode = ? WHERE id = ?")
                .execute(Tuple.of(name_R, fiscalNumber, address, postalCode, id_R))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

}
