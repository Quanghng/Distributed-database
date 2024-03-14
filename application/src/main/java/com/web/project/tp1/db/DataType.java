package com.web.project.tp1.db;

public enum DataType {

    STRING(String.class),
    SHORT(Short.class),
    INT(Integer.class);

    public final Class<?> type;

    DataType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
