package org.acme;

public class SupplierType {
    private final SupplierID id;
    private final String name;

    public SupplierType(SupplierID id, String name) {
        if (id == null || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Supplier ID and name cannot be null or empty.");
        }
        this.id = id;
        this.name = name;
    }

    public SupplierID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "{id: " + id + ", name: \"" + name + "\"}";
    }
}
