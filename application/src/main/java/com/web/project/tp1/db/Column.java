package com.web.project.tp1.db;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.web.project.tp1.storage.BasicStorage;
import com.web.project.tp1.storage.Storage;

public class Column<T> {
    private final String columnName;
    private final DataType type;

    private final BasicStorage<T> storage = new BasicStorage<>();

    @JsonCreator
    public Column(@JsonProperty("columnName") String columnName, @JsonProperty("type") DataType type, @JsonProperty("storage") BasicStorage<T> storage) {
        this.columnName = columnName;
        this.type = type;
        if(storage!=null) {
            storage.getData().forEach(data -> { this.put((T) data);});
        }
    }

    public String getColumnName() {return columnName;}
    public DataType getType() {
        return type;
    }
    public Storage getStorage() {return storage;}

    public void put(T value){
        storage.put(value);
    }

}
