package com.boardwise.backend.marketplace.enums;

public enum ItemType {
    MERCHANDISE("merchandise"),
    EXPANSION("expansion"),
    BOARDGAME("boardgame");

    private final String value;

    ItemType(String value) {
        this.value = value;
    }

    public static ItemType fromValue(String value) {
        for (ItemType type : values()) {
            if (type.value.equalsIgnoreCase(value))
                return type;
        }
        throw new IllegalArgumentException("Unknown genre value: " + value);
    }
}
