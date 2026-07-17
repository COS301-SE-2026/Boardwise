package com.boardwise.backend.marketplace.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    RENTAL("rental"),
    SALE("sale");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order status value: " + value);
    }
}