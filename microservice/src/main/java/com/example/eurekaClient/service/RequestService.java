package com.example.eurekaClient.service;

import com.example.eurekaClient.controller.Condition;
import com.example.eurekaClient.converter.StringConvertToObject;
import com.example.eurekaClient.db.Column;
import com.example.eurekaClient.db.Table;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RequestService {

    public List<Integer> getColumnWithConditions(Table table, List<Condition> conditions, Integer to, Integer at) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        if(conditions==null ||conditions.isEmpty()){
            return this.getAllRowsIndex(table);
        }
        List<Integer> indexResults = getIndexOfRowWithTheFirstCondition(table,conditions.get(0));
        for( Condition condition : conditions) {
            indexResults = this.getIndexOfRowWithCondition(table,indexResults, condition);
        }
        if(to!=null && at!=null && to<=at && to<indexResults.size() ){
            indexResults.subList(to, indexResults.size() < at ? indexResults.size() : at );
        }
        return indexResults;
    }

    public List<Column> getColumns(Table table, List<String> selectColumns ) {
        if(selectColumns!=null && !selectColumns.isEmpty()) {
            List<Column> columns = table.getColumns().stream()
                    .filter(column -> selectColumns.contains(column.getColumnName()))
                    .collect(Collectors.toList());
            Collections.sort(columns, Comparator.comparingInt(column -> selectColumns.indexOf(column.getColumnName())));
            return columns;
        } else {
            return table.getColumns();
        }
    }

    public List<List<Object>> selectColumns(List<Column> columns, List<Integer> indexData) {
        return indexData.stream().mapToInt(Integer::intValue).mapToObj(index -> columns.stream()
                        .map(column -> column.getStorage().getData().get(index)).collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private <T> List<Integer> getIndexOfRowWithTheFirstCondition(Table table, Condition condition) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        List<Column> list = table.getColumns().stream()
                .filter(column -> condition.columnName().equalsIgnoreCase(column.getColumnName()))
                .collect(Collectors.toList());

        if(list.isEmpty()){
            throw new IllegalArgumentException("the column '%s' does not exist".formatted(condition.columnName()));
        }
        Column column = list.get(0);
        final List<T> storage = column.getStorage().getData();
        final var valueParsed = new StringConvertToObject()
                .convert(column.getType().type,
                        condition.value());

        return IntStream.range(0, column.getStorage().getData().size())
                .filter(data -> {
                    try {
                        return condition.compareTo(storage.get(data), valueParsed);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .boxed()
                .toList();
    }

    private <T> List<Integer> getIndexOfRowWithCondition(Table table, List<Integer> indexOfDataResults, Condition condition) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        List<Column> list = table.getColumns().stream()
                .filter(column -> condition.columnName().equalsIgnoreCase(column.getColumnName()))
                .collect(Collectors.toList());

        if(list.isEmpty()){
            throw new IllegalArgumentException("the column '%s' does not exist".formatted(condition.columnName()));
        }
        Column column = list.get(0);
        final List<T> storage = column.getStorage().getData();
        final var valueParsed = new StringConvertToObject()
                .convert(column.getType().type,
                        condition.value());

        return indexOfDataResults.stream().mapToInt(Integer::intValue)
                .filter(data -> {
            try {
                return condition.compareTo(storage.get(data), valueParsed);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).boxed().collect(Collectors.toList());
    }


    private List<Integer> getAllRowsIndex(Table table) {
        return IntStream
                .range(0, table.getColumns().get(0).getStorage().getData().size())
                .boxed()
                .collect(Collectors.toList());
    }
}
