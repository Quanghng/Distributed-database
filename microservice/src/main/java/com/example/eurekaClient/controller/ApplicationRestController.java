package com.example.eurekaClient.controller;

import com.example.eurekaClient.db.Column;
import com.example.eurekaClient.db.Table;
import com.example.eurekaClient.service.MicroserviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

@RestController
public class ApplicationRestController {

    @Autowired
    private MicroserviceService service;

    @PostMapping(path = "/table", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Table> createTable(@RequestBody Table table){
        System.out.println("table "+table.getName() + " created");
        service.createTable(table);
        return new ResponseEntity<>(table, HttpStatus.CREATED);
    }

    @PostMapping(path = "/insert/{table}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> insertDataFromCsvFile(@PathVariable("table") String tableName,
                                                         @RequestBody List<List<String>> lines) {
        try {
            System.out.println("data inserted");
            service.insertData(tableName,lines);
        } catch(Exception e) {
            return new ResponseEntity<>(false, HttpStatus.NOT_IMPLEMENTED);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping(path = "/{table}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> select(@PathVariable("table") String table,
                                    @RequestParam(value = "select", required = false) List<String> selectColumns,
                                    @RequestParam(value = "where", required = false) List<Condition> where,
                                    @RequestParam(value = "to", required=false) Integer to,
                                       @RequestParam(value="at", required = false) Integer at) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        System.out.println("select "+table);
        new HttpHeaders();
        final List result = service.selectWhere(table,
                selectColumns,
                where,
                to,
                at);
        HttpHeaders headers = new HttpHeaders();
        headers.addAll("Content-Type", Collections.singletonList(MediaType.APPLICATION_JSON_VALUE));

        if(to!=null && at!=null && result.size()==to-at){
            headers.addIfAbsent("next","yes");
        } else {
            headers.addIfAbsent("next","no");
        }
        return new ResponseEntity<>(
                result,
                headers,
                HttpStatus.OK);
    }

    @GetMapping(path = "/select/{table}", produces = "application/json")
    public ResponseEntity<Table> selectAll(@PathVariable("table") String table) {
        try {
            Table selectedTable = service.selectAll(table);
            if (selectedTable != null) {
                return new ResponseEntity<>(selectedTable, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_IMPLEMENTED);
        }
    }

    @PostMapping(path = "/addCol/{table}", consumes = "application/json")
    public ResponseEntity<Boolean> addColumn(@PathVariable("table") String tableName,
                                             @RequestBody Column<?> newColumn) {
        try {
            System.out.println("add Column");
            service.addColumn(tableName, newColumn);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @DeleteMapping(path = "/delCol/{table}")
    public ResponseEntity<Boolean> deleteColumn(@PathVariable("table") String tableName,
                                             @RequestParam("colName") String columnName) {
        try {
            System.out.println("delete Column");
            service.deleteColumn(tableName, columnName);
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping(path = "/countCol/{table}")
    public ResponseEntity<Integer> countColumn(@PathVariable("table") String tableName) {
        int result;
        try {
            System.out.println("count Column");
            result = service.countColumn(tableName);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/sum/{table}")
    public ResponseEntity<Float> sumColumn(@PathVariable("table") String tableName,
                                               @RequestParam("colName") String colName) {
        float result;
        try {
            System.out.println("sum Column");
            result = service.sumColumn(tableName, colName);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(path = "/avg/{table}")
    public ResponseEntity<Float> avgColumn(@PathVariable("table") String tableName,
                                             @RequestParam("colName") String colName) {
        float result;
        try {
            System.out.println("avg Column");
            result = service.avgColumn(tableName, colName);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    @GetMapping(path = "/max/{table}")
    public ResponseEntity<Object> maxColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        try {
            System.out.println("max Column");
            var result = service.maxColumn(tableName, colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/min/{table}")
    public ResponseEntity<Object> minColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        try {
            System.out.println("min Column");
            var result = service.minColumn(tableName, colName);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
