package com.web.project.tp1.db;

import com.web.project.tp1.storage.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {
    private String name;
    private final List<Column> columns = new ArrayList<>();
    private Storage storage;
    private final Map<String, List<?>> storageInfo = new HashMap<>();
    public String getName() {
        return name;
    }
    public List<Column> getColumns() {
        return columns;
    }
    public <T> void put(Column<T> col, T cell) {
        col.put(cell);
        String columnName = col.getColumnName();
        List<T> storageData = col.getStorage().getData();
        storageInfo.put(columnName, storageData);
    }
}
