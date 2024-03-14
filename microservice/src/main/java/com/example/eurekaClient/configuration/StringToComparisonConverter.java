package com.example.eurekaClient.configuration;

import com.example.eurekaClient.controller.Comparison;
import org.springframework.core.convert.converter.Converter;

public class StringToComparisonConverter implements Converter<String, Comparison> {

    @Override
    public Comparison convert(String value) {
        return Comparison.fromValue(value);
    }
}



