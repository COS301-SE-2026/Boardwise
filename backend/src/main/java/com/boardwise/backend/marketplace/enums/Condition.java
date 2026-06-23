package com.boardwise.backend.marketplace.enums;

public enum Condition {
    New("new"),
    LIKE_NEW("like new"),
    GOOD("good"),
    FAIR("fair");

    private final String value;

    Condition(String value) {
        this.value = value;
    };

    public String getValue() {
        return value;
    }

    public boolean isEqual(String x) {
        return value.equals(x); 
    }

    public static Condition fromValue(String value) {
        for (Condition type : values()) {
            if (type.value.equalsIgnoreCase(value))
                return type;
        }
        throw new IllegalArgumentException("Unknown genre value: " + value);
    }
}
