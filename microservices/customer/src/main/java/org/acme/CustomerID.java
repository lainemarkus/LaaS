package org.acme;

public class CustomerID {

    private final Long value;

    public CustomerID(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Customer ID must be positive");
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
