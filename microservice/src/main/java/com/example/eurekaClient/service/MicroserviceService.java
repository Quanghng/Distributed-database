package com.example.eurekaClient.service;

import com.example.eurekaClient.controller.Condition;
import com.example.eurekaClient.db.Column;
import com.example.eurekaClient.db.DataType;
import com.example.eurekaClient.db.Database;
import com.example.eurekaClient.db.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Service
public class MicroserviceService {

    @Autowired
    private Database database;

    @Autowired
    private RequestService requestService;

    public void createTable(Table table){
        database.put(table);
    }

    public void insertData(String tableName, List<List<String>> linesData) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Table table = database.get(tableName);
        // Start after column name
        for (List<String> row : linesData) {
            for (int i = 0; i < row.size(); i++) {
                // We are in an unique table
                Column column = table.getColumns().get(i);
                if(row.get(i).isBlank()){
                    table.put(column, null);
                    continue;
                }
                final Class<?> classConverted = column.getType().type;
                final Constructor<?> constructor = classConverted.getConstructor(String.class);

                var valueParsed = constructor.newInstance(row.get(i));
                table.put(column, valueParsed);
            }
        }
    }

    public Table selectAll(String table){
        return database.get(table);
    }

    public List<List<Object>> selectWhere(String tableName,
                                          List<String> selectColumns,
                                          List<Condition> conditions,
                                          Integer to,
                                          Integer at) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Table table = database.get(tableName);
        final List<Integer> indexResultsWithWhere = requestService.getColumnWithConditions(table,conditions, to, at);
        final List<Column> selectColumnsResults = requestService.getColumns(table,selectColumns);
        return requestService.selectColumns(selectColumnsResults,indexResultsWithWhere);
    }

    public boolean addColumn(String tableName, Column<?> newColumn) {
        boolean res = false;
        try {
            Table selectedTable = database.get(tableName);
            for (Column column : selectedTable.getColumns()) {
                if (column.getColumnName().equalsIgnoreCase(newColumn.getColumnName())) {
                    throw new IllegalArgumentException("Column already existed !");
                }
            }
            for (int i = 0; i < selectedTable.getColumns().get(0).getStorage().getData().size(); i++)
                newColumn.put(null);
            selectedTable.getColumns().add(newColumn);
            res = true;
        } catch (Exception e){
            res = false;
        }
        return res;
    }

    public boolean deleteColumn(String tableName, String columnName) {
        boolean res = false;
        try {
            List<Column> columns = database.get(tableName).getColumns();
            for (Column column : columns) {
                if (column.getColumnName().equalsIgnoreCase(columnName)) {
                    columns.remove(column);
                    res = true;
                    break;
                }
            }
            if (res == false) {
                throw new IllegalArgumentException("Column doesn't exist !");
            }
        } catch (Exception e) {
            res = false;
        }
        return res;
    }

    public int countColumn(String tableName) {
        return database.get(tableName).getColumns().size();
    }

    public float sumColumn(String tableName, String colName) {
        float res = 0;
        Table selectedTable = database.get(tableName);
        List<Column> columns = selectedTable.getColumns();
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                if ((column.getType() == DataType.SHORT) || (column.getType() == DataType.INT) || (column.getType() == DataType.FLOAT)) {
                    for(Object elem : column.getStorage().getData()){
                        if (elem instanceof Short) {
                            res += (Short) elem;
                        } else if (elem instanceof Integer) {
                            res += (Integer) elem;
                        } else if (elem instanceof Float) {
                            res += (Float) elem;
                        }
                    }
                    return res;
                }
                else {
                    throw new IllegalArgumentException("Column must be type SHORT or INT");
                }
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }

    public Float avgColumn(String tableName, String colName) {
        Float sum = sumColumn(tableName, colName);
        if (sum == null) {
            return null;  // Return null if the sum is null
        }
        return (sum / database.get(tableName).getColumns().size());
    }

    public Object maxColumn(String tableName, String colName) {
        Table selectedTable = database.get(tableName);
        List<Column> columns = selectedTable.getColumns();
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                if ((column.getType() == DataType.SHORT) || (column.getType() == DataType.INT) || (column.getType() == DataType.FLOAT)) {
                    float maxVal = 0;
                    boolean firstVal = true;

                    for(Object elem : column.getStorage().getData()){
                        if (elem instanceof Number) {
                            float floatElem = ((Number) elem).floatValue();
                            if (firstVal || floatElem > maxVal) {
                                maxVal = floatElem;
                                firstVal = false;
                            }
                        }
                    }
                    return maxVal;
                }
                else if (column.getType() == DataType.STRING) {
                    String maxVal = "";
                    boolean firstVal = true;
                    for(Object elem : column.getStorage().getData()){
                        if (elem instanceof String) {
                            String strElem = (String) elem;
                            if (firstVal || strElem.compareTo(maxVal) > 0) {
                                maxVal = strElem;
                                firstVal = false;
                            }
                        }
                    }
                    return maxVal;
                }
                else {
                    throw new IllegalArgumentException("Unsupported data type!");
                }
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }

    public Object minColumn(String tableName, String colName) {
        Table selectedTable = database.get(tableName);
        List<Column> columns = selectedTable.getColumns();
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                if ((column.getType() == DataType.SHORT) || (column.getType() == DataType.INT) || (column.getType() == DataType.FLOAT)) {
                    float minVal = 0;
                    boolean firstVal = true;

                    for(Object elem : column.getStorage().getData()){
                        if (elem instanceof Number) {
                            float floatElem = ((Number) elem).floatValue();
                            if (firstVal || floatElem < minVal) {
                                minVal = floatElem;
                                firstVal = false;
                            }
                        }
                    }
                    return minVal;
                }
                else if (column.getType() == DataType.STRING) {
                    String minVal = "";
                    boolean firstVal = true;
                    for(Object elem : column.getStorage().getData()){
                        if (elem instanceof String) {
                            String strElem = (String) elem;
                            if (firstVal || strElem.compareTo(minVal) < 0) {
                                minVal = strElem;
                                firstVal = false;
                            }
                        }
                    }
                    return minVal;
                }
                else {
                    throw new IllegalArgumentException("Unsupported data type!");
                }
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }
}
