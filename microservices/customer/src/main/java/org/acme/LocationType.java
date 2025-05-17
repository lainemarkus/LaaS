package org.acme;

public class LocationType {

    private final String address;
    private final PostalCodeType postalCode;

    public LocationType(String address, PostalCodeType postalCode) {
        this.address = address;
        this.postalCode = postalCode;
    }

    public String getAddress() {
        return this.address;
    }

    public PostalCodeType getPostalCode() {
        return this.postalCode;
    }

    @Override
    public String toString() {
        return this.address + ", " + this.postalCode;
    }
}
