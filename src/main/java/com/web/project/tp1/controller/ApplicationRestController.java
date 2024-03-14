package com.web.project.tp1.controller;

import com.web.project.tp1.db.Column;
import com.web.project.tp1.db.Database;
import com.web.project.tp1.db.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Constructor;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ApplicationRestController {

    @Autowired
    private Database database;

    @PostMapping(path = "/table", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Table> createTable(@RequestBody Table table){
        database.put(table);
        return new ResponseEntity<>(table, HttpStatus.CREATED);
    }


    @PostMapping(path = "/insert/{table}", consumes = "text/csv")
    public ResponseEntity<Boolean> insertDataFromCsvFile(@PathVariable("table") String tableName,
                                                         @RequestParam("skip") @DefaultValue("0") int skip,
                                                         @RequestBody String csv) {
        try {
            Table table = database.get(tableName);
            final String[] lines = csv.split("\n");
            // Start after column name
            for(int j = skip ; j < lines.length ; j++) {
                final var columns = lines[j].split(",");
                for (int i = 0 ; i < columns.length ; i++) {
                    // We are in an unique table
                    Column column = table.getColumns().get(i);
                    final Class<?> classConverted = column.getType().type;
                    final Constructor<?> constructor = classConverted.getConstructor(String.class);

                    var valueParsed = constructor.newInstance(columns[i]);
                    table.put(column, valueParsed);
                }
            }
        } catch(Exception e) {
            return new ResponseEntity<>(false, HttpStatus.NOT_IMPLEMENTED);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    // Select all tables
//    @GetMapping(path = "/select", produces = "application/json")
//    public ResponseEntity<Database> selectAll() {
//        try {
//            return new ResponseEntity<>(database, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
//        }
//    }

    @GetMapping(path = "/{table}", produces = "application/json")
    public ResponseEntity<Table> selectTable(@PathVariable("table") String table) {
        try {
            Table selectedTable = database.get(table);
            if (selectedTable != null) {
                return new ResponseEntity<>(selectedTable, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch(Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @GetMapping(path = "/selectById/{table}", produces = "application/json")
    public ResponseEntity<Map<String, Object>> selectRowById(@PathVariable("table") String table,
                                                             @RequestParam("id") int id) {
        try {
            Table selectedTable = database.get(table);
            if (selectedTable != null) {
                Map<String, Object> rowData = selectedTable.getRowById(id);
                if (rowData != null) {
                    return new ResponseEntity<>(rowData, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @PostMapping(path = "/addColumn/{table}", consumes = "application/json")
    public ResponseEntity<Boolean> addColumn(@PathVariable("table") String tableName,
                                             @RequestBody Column<?> column) {
        try {
            Table table = database.get(tableName);
            return new ResponseEntity<>(table.addColumn(column), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(path = "/deleteColumn/{table}")
    public ResponseEntity<Boolean> deleteColumn(@PathVariable("table") String tableName,
                                                @RequestParam("ind") int ind) {
        try {
            Table table = database.get(tableName);
            return new ResponseEntity<>(table.deleteColumn(ind), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/sum/{table}")
    public ResponseEntity<Float> sumColumn(@PathVariable("table") String tableName,
                                             @RequestParam("colName") String colName) {
        try {
            Table table = database.get(tableName);
            Float result = table.sumColumn(colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/avg/{table}")
    public ResponseEntity<Float> avgColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        try {
            Table table = database.get(tableName);
            Float result = table.avgColumn(colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/min/{table}")
    public ResponseEntity<Object> minColumn(@PathVariable("table") String tableName,
                                           @RequestParam("colName") String colName) {
        try {
            Table table = database.get(tableName);
            var result = table.minColumn(colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/max/{table}")
    public ResponseEntity<Object> maxColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        try {
            Table table = database.get(tableName);
            var result = table.maxColumn(colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/count/{table}")
    public ResponseEntity<Integer> countColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        try {
            Table table = database.get(tableName);
            Integer result = table.countColumn(colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

