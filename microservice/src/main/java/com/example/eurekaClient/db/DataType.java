package com.example.eurekaClient.db;

public enum DataType {

    STRING(String.class),
    SHORT(Short.class),
    INT(Integer.class),
    FLOAT(Float.class);;

    public final Class<?> type;

    DataType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
