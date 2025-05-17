package org.acme.common;

public class LoyaltyCardID {
    private final Long value;

    public LoyaltyCardID(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("LoyaltyCard ID must be positive");
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
