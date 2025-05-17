package org.acme;

public class ProductType {
    private final String name;
    private final SupplierType supplier;

    public ProductType(String name, SupplierType supplier) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name must not be null or empty.");
        }
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier must not be null.");
        }
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return name;
    }

    public SupplierType getSupplier() {
        return supplier;
    }

    @Override
    public String toString() {
        return "{name: \"" + name + "\", supplier: " + supplier + "}";
    }
}
