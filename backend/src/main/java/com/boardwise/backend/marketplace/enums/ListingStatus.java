package com.boardwise.backend.marketplace.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingStatus {
    AVAILABLE("available"),
    RENTED("rented"),
    SOLD("sold");

    private final String value;

    ListingStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
    

    public static ListingStatus fromValue(String value) {
        for (ListingStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown listing status value: " + value);
    }
}