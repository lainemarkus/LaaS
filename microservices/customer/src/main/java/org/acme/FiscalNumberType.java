package org.acme;

public class FiscalNumberType {

    private final String value;

    public FiscalNumberType(String value) {
        if (value == null || !value.matches("\\d{9}")) { // Simple validation: 9 digits
            throw new IllegalArgumentException("Invalid fiscal number format");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
