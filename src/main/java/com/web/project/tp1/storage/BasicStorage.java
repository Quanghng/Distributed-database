package com.web.project.tp1.storage;

import java.util.ArrayList;
import java.util.List;

public class BasicStorage<T> implements Storage<T> {
    private final List<T> columnRowSet = new ArrayList<>();

    public List<T> getData() {return columnRowSet;}

    @Override
    public void put(T row) {
        columnRowSet.add(row);
    }

    public T get(int id) {
        return columnRowSet.get(id);
    }

    public int getSize() {return columnRowSet.size();}
}
