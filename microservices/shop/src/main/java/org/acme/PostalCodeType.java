package org.acme;

public class PostalCodeType {

    private final String value;

    public PostalCodeType(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{3}")) {
            throw new IllegalArgumentException("Invalid postal code format");
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
