package com.web.project.tp1.controller;

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

    @Override
    public String toString() {
        return "%s,%s,%s".formatted(columnName,comparison.toString(),value);
    }
}
