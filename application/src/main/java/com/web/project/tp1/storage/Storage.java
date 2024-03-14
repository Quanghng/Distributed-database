package com.web.project.tp1.storage;

import java.util.List;

public interface Storage<T> {

    void put(T row);

    <T> List<T> getData();
}
