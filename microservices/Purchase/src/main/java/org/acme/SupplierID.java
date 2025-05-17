package org.acme;

public class SupplierID {
    private final Long value;

    public SupplierID(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Supplier ID must be positive");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
