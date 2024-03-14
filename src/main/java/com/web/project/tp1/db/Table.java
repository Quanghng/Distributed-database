package com.web.project.tp1.db;

import com.web.project.tp1.storage.Storage;

import java.util.*;

public class Table {
    private String name;
    private final List<Column> columns = new ArrayList<>();
    private final List<Storage> storages = new ArrayList<>();

    public String getName() {
        return name;
    }
    public List<Column> getColumns() {
        return columns;
    }
    public <T> void put(Column<T> col, T cell) {
        col.put(cell);
        if (storages.size() < columns.size())
            storages.add(col.getStorage());
    }

    public Map<String, Object> getRowById(int id) {
        if (id < 0 || id >= storages.get(0).getSize()) {
            return null; // Invalid ID
        }

        Map<String, Object> rowData = new HashMap<>();

        for (int i = 0; i < columns.size(); i++) {
            Column<?> column = columns.get(i);
            Storage<?> storage = storages.get(i);

            if (storage.getSize() == 0) {
                rowData.put(column.getColumnName(), "");
            } else {
                rowData.put(column.getColumnName(), storage.get(id));
            }
        }

        return rowData;
    }

    public boolean addColumn(Column<?> newColumn) {
        boolean res = false;
        try {
            columns.add(newColumn);
            storages.add(newColumn.getStorage());
            res = true;
        } catch (Exception e){
            res = false;
        }
        return res;
    }

    public boolean deleteColumn(int columnIndex) {
        boolean res = false;
        if (columnIndex >= 0 && columnIndex < columns.size()) {
            columns.remove(columnIndex);
            storages.remove(columnIndex);
            res = true;
        }
        return res;
    }

    public float sumColumn(String colName) {
        float res = 0;
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

    public Float avgColumn(String colName) {
        Float sum = sumColumn(colName);
        if (sum == null) {
            return null;  // Return null if the sum is null
        }
        return (sum / columns.size());
    }

    public Object minColumn(String colName) {
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
                    throw new IllegalArgumentException("Unsupported data type: " + column.getStorage().get(0).getClass().getSimpleName());
                }
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }

    public Object maxColumn(String colName) {
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
                    throw new IllegalArgumentException("Unsupported data type: " + column.getStorage().get(0).getClass().getSimpleName());
                }
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }

    public int countColumn(String colName) {
        for (Column column : columns) {
            if (column.getColumnName().equals(colName)) {
                return column.getStorage().getSize();
            }
        }
        throw new IllegalArgumentException("Column not found: " + colName);
    }
}
