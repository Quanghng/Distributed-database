package com.web.project.tp1.db;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class Database {

    private static final Map<String, Table> tables = new ConcurrentHashMap<>();

    public void put(Table table) {
        if (tables.containsKey(table.getName())) {
            throw new IllegalArgumentException("Name already exists !");
        }
        tables.put(table.getName(), table);
    }

    public Table get(String name) {
        return tables.get(name);
    }
}
