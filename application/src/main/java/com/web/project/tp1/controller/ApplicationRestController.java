package com.web.project.tp1.controller;

import com.opencsv.CSVReader;
import com.web.project.tp1.db.Column;
import com.web.project.tp1.db.Table;
import com.web.project.tp1.service.DistribuedServersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/api/v2")
public class ApplicationRestController {

    @Autowired
    private DistribuedServersService service;

    @PostMapping(path = "/table", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Table> createTable(@RequestBody Table table) {
        return service.createTableResponse(table);
    }


    @PostMapping(path = "/insert/{table}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Boolean> insertDataFromCsvFile(@PathVariable("table") String tableName,
                                                         @RequestParam("skip") @DefaultValue("0") int skip,
                                                         @RequestParam("file") MultipartFile csv) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(csv.getInputStream()))) {
            reader.skip(skip);
            return service.insertDataResponse(tableName,reader);
        } catch(Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/{table}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List> selectWhere(@PathVariable("table") String table,
                                            @RequestParam(value = "select", required = false) List<String> selectColumns,
                                            @RequestParam(value = "where", required = false) List<Condition> whereRequest,
                                            @RequestParam(value = "limit", required = false) Integer limitRows ) {
        return service.selectDataResponse(table,selectColumns,whereRequest, limitRows);
    }

    @GetMapping(path = "/countCol/{table}")
    public ResponseEntity<Integer> countColumn(@PathVariable("table") String tableName) {
        return service.countColumn(tableName);
    }

    @GetMapping(path = "/sum/{table}")
    public ResponseEntity<Float> sumColumn(@PathVariable("table") String tableName,
                                           @RequestParam("colName") String colName) {
        return service.sumColumn(tableName, colName);
    }

    @GetMapping(path = "/avg/{table}")
    public ResponseEntity<Float> avgColumn(@PathVariable("table") String tableName,
                                           @RequestParam("colName") String colName) {
        return service.avgColumn(tableName, colName);
    }

    @GetMapping(path = "/max/{table}")
    public ResponseEntity<Object> maxColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        return service.maxColumn(tableName, colName);
    }

    @GetMapping(path = "/min/{table}")
    public ResponseEntity<Object> minColumn(@PathVariable("table") String tableName,
                                            @RequestParam("colName") String colName) {
        return service.minColumn(tableName, colName);
    }

    @PostMapping(path = "/addCol/{table}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> addColumn(@PathVariable("table") String tableName ,@RequestBody Column<?> column) {
        return service.addColumn(tableName, column);
    }

    @DeleteMapping(path = "/delCol/{table}")
    public ResponseEntity<Boolean> addColumn(@PathVariable("table") String tableName ,@RequestParam("colName") String columnName) {
        return service.deleteColumn(tableName, columnName);
    }
}
