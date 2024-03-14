package com.web.project.tp1.controller;

public enum Comparison {
    EQUALS("="),
    GREATER(">"),
    LESS("<");

    private final String value;

    Comparison(String s) {
        this.value = s;
    }

    public String getValue() {
        return value;
    }

    public static Comparison fromValue(String value) {
        for (Comparison comparison : Comparison.values()) {
            if (comparison.getValue().equals(value)) {
                return comparison;
            }
        }
        throw new IllegalArgumentException("Invalid comparison value: " + value);
    }
}
