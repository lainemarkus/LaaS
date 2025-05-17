package org.acme;

import java.util.Objects;

public class DiscountType {
    private final String code;
    private final Double amount;

    public DiscountType(String code, Double amount) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Discount code must not be empty");
        }
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }

        this.code = code;
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public Double getAmount() {
        return amount;
    }

    // Returns the numeric discount value (used in SQL insert/update)
    public Double getValue() {
        return amount;
    }

    @Override
    public String toString() {
        return code + " (" + amount + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiscountType)) return false;
        DiscountType that = (DiscountType) o;
        return code.equals(that.code) && amount.equals(that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, amount);
    }
}
