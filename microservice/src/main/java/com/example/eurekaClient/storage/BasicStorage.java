package com.example.eurekaClient.storage;

import java.util.ArrayList;
import java.util.List;

public class BasicStorage<T> implements Storage<T> {

    private final List<T> columnRowSet = new ArrayList<>();

    @Override
    public List<T> getData() {return columnRowSet;}

    @Override
    public void put(T row) {
        columnRowSet.add(row);
    }
}
