package com.example.eurekaClient.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.InternalServerErrorException;

import java.lang.reflect.InvocationTargetException;

public record Condition(String columnName, Comparison comparison, String value) {


    @JsonCreator
    public Condition(@JsonProperty("columnName") String columnName,
                     @JsonProperty("comparison") Comparison comparison,
                     @JsonProperty("value") String value) {
        this.columnName = columnName;
        this.comparison = comparison;
        this.value = value;
    }

    public <T> boolean compareTo(T o1, T o2) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        return switch (comparison) {
            case LESS -> (int) o1.getClass().getMethod("compare",
                    (Class<?>) o1.getClass().getField("TYPE").get(null),
                    (Class<?>) o2.getClass().getField("TYPE").get(null)).invoke(null, o1, o2) < 0;
            case EQUALS -> o1.equals(o2);
            case GREATER -> (int) o1.getClass().getMethod("compare",
                    (Class<?>) o1.getClass().getField("TYPE").get(null),
                    (Class<?>) o2.getClass().getField("TYPE").get(null)).invoke(null, o1, o2) > 0;
        };
    }
}
