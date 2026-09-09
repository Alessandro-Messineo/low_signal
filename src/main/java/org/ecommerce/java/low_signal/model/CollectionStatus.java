package org.ecommerce.java.low_signal.model;

public enum CollectionStatus {
    AVAILABLE("AVAILABLE"),
    ARCHIVE("ARCHIVE"),
    SOLD_OUT("SOLD OUT");

    private final String value;

    CollectionStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}   