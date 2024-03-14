package com.web.project.tp1.configuration;

import com.web.project.tp1.controller.Comparison;
import com.web.project.tp1.controller.Condition;
import org.springframework.core.convert.converter.Converter;

public class StringToConditionConverter implements Converter<String, Condition> {

    public Condition convert(String value) {
        final String[] valueSplited = value.split(",");
        if(valueSplited.length!=3) {
            throw new IllegalArgumentException("Condition requires 3 parameters");
        }
       return new Condition(valueSplited[0], Comparison.valueOf(valueSplited[1]),valueSplited[2]);
    }
}
