package com.web.project.tp1.configuration;

import com.web.project.tp1.controller.Comparison;
import org.springframework.core.convert.converter.Converter;

public class StringToComparisonConverter implements Converter<String, Comparison> {

    @Override
    public Comparison convert(String value) {
        return Comparison.fromValue(value);
    }
}



