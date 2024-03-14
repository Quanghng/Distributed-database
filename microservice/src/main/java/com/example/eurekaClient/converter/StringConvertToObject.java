package com.example.eurekaClient.converter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StringConvertToObject {

    public <T> T convert(Class<T> convertClass, String value) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final Constructor<?> constructor = convertClass.getConstructor(String.class);
        return (T) constructor.newInstance(value);
    }
}
