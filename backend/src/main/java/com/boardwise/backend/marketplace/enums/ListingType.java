package com.boardwise.backend.marketplace.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingType {
    RENTAL("rental"),
    SALE("sale");

    private final String value;

    ListingType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ListingType fromValue(String value) {
        for (ListingType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown listing type value: " + value);
    }
}