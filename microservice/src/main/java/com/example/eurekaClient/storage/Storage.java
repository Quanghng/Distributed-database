package com.example.eurekaClient.storage;

import java.util.List;

public interface Storage<T> {

    void put(T row);

    <T> List<T> getData();
}
