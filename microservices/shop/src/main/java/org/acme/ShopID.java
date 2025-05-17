package org.acme;

public class ShopID {
    private final Long value;

    public ShopID(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Shop ID must be positive");
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
