package com.web.project.tp1.db;

import com.web.project.tp1.storage.BasicStorage;
import com.web.project.tp1.storage.Storage;

public class Column<T> {
    private final String columnName;
    private DataType type;
    private final Storage<T> storage = new BasicStorage<>();

    public Column(String columnName, DataType type) {
        this.columnName = columnName;
        this.type = type;
    }

    public String getColumnName() {return columnName;}
    public DataType getType() {
        return type;
    }
    public Storage getStorage() {return storage;}
    public T getDataById(int id) {return storage.get(id);}
    public void put(T value){
        storage.put(value);
    }

}
