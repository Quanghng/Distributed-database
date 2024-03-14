package com.example.eurekaClient.db;

import com.example.eurekaClient.storage.BasicStorage;
import com.example.eurekaClient.storage.Storage;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Column<T> {
    private final String columnName;
    private final DataType type;

    private final BasicStorage<T> storage = new BasicStorage<>();

    @JsonCreator
    public Column(@JsonProperty("columnName") String columnName, @JsonProperty("type") DataType type, @JsonProperty("storage") BasicStorage<T> storage) {
        this.columnName = columnName;
        this.type = type;
        if(storage!=null) {
            storage.getData().forEach(data -> this.put((T) data));
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
