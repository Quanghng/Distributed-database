package com.web.project.tp1.storage;

import java.util.List;

public interface Storage<T> {

    void put(T row);

    public T get(int id);

    public int getSize();

    public List<T> getData();

}
