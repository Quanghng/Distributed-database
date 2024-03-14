package com.web.project.tp1.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.opencsv.CSVReader;
import com.web.project.tp1.controller.Condition;
import com.web.project.tp1.db.Column;
import com.web.project.tp1.db.Table;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class DistribuedServersService {

    @Lazy
    @Autowired
    private EurekaClient eurekaClient;

    private static final String MICROSERVICE_NAME = "microservice";

    private static final String CREATE_TABLE_PATH = "table";

    private static final String INSERT_DATA_PATH = "insert/";

    private static final String SELECT_DATA_PATH = "";

    private static final String COUNT_COLUMN_PATH = "countCol/";

    private static final String ADD_COLUMN_PATH = "addCol/";

    private static final String DEL_COLUMN_PATH = "delCol/";

    private static final String SUM_COLUMN_PATH = "sum/";

    private static final String AVG_COLUMN_PATH = "avg/";

    private static final String MAX_COLUMN_PATH = "max/";

    private static final String MIN_COLUMN_PATH = "min/";

    private static final int NUMBER_LINES_DATA_TO_SEND = 800;


    public ResponseEntity<Table> createTableResponse(Table table) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        try {
            for( InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URI uri = new URI(url + CREATE_TABLE_PATH);
                ResponseEntity<Table> response = restTemplate.postForEntity(uri, table, Table.class);
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new RuntimeException(response.toString());
                }
                System.out.println("request sent to" +instanceInfo.getHomePageUrl());
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(table,HttpStatus.CREATED);
    }

    public synchronized ResponseEntity<Boolean> insertDataResponse(String tableName, CSVReader data) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(false,HttpStatus.RESET_CONTENT);
        }
        try {
            while(true) {
                List<Future> futures = new ArrayList<>();
                int i=0;
                /* todo : faire varier un nombre max pour chaque microservice
                    et calculer son temps
                    ensuite faire un graphe, et choisir un nombre idéal?
                 */
                for( InstanceInfo instanceInfo : microservices) {
                    String[] nextLine;
                    i = 0;
                    final List<List<String>> lines = new ArrayList<>();
                    while ((nextLine = data.readNext()) != null && i<NUMBER_LINES_DATA_TO_SEND) {
                        lines.add(Arrays.stream(nextLine).toList());
                        i++;
                    }
                    final Future<Void> future = CompletableFuture.runAsync(() -> {
                        String url = instanceInfo.getHomePageUrl();
                        RestTemplate restTemplate = new RestTemplate();
                        URI uri;
                        try {
                            uri = new URI(url + INSERT_DATA_PATH + tableName);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        ResponseEntity<Boolean> response = restTemplate.postForEntity(uri, lines, Boolean.class);
                        if (!response.getStatusCode().is2xxSuccessful()) {
                            throw new RuntimeException(response.toString());
                        }
                        System.out.println("request sended to" + instanceInfo.getHomePageUrl());
                    });
                    futures.add(future);
                }
                if(i!=NUMBER_LINES_DATA_TO_SEND){
                    break;
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        } catch (Exception e) {
        return new ResponseEntity<>(false,HttpStatus.INTERNAL_SERVER_ERROR);
    }
        return new ResponseEntity<>(true,HttpStatus.OK);
    }

    public ResponseEntity<List> selectDataResponse(String tableName,
                                                   List<String> selectColumns,
                                                   List<Condition> whereRequest,
                                                   Integer limitRows) {
        int nextNumber = 0;
        List<Future> futures = new ArrayList<>();
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(null,HttpStatus.RESET_CONTENT);
        }
        List result = Collections.synchronizedList(new ArrayList());
        try {
            List<AtomicBoolean> next;
            do {
                 next = new ArrayList<>();
                for( InstanceInfo instanceInfo : microservices) {
                    String url = instanceInfo.getHomePageUrl();
                    RestTemplate restTemplate = new RestTemplate();
                    URIBuilder uri = new URIBuilder(url + SELECT_DATA_PATH + tableName);

                    if(selectColumns!=null && !selectColumns.isEmpty()){
                       uri.addParameter("select", String.join(",",selectColumns));
                    }
                    if(whereRequest != null && !whereRequest.isEmpty()) {
                        whereRequest.forEach(where ->  uri.addParameter("where", where.toString()));
                    }
                    uri.addParameter("to",String.valueOf(nextNumber));
                    uri.addParameter("at",String.valueOf(nextNumber+NUMBER_LINES_DATA_TO_SEND));
                    List<AtomicBoolean> finalNext = next;
                    List finalResult = result;
                    final Future<Void> future = CompletableFuture.runAsync(() -> {
                        ResponseEntity<List> response = null;
                        try {
                            System.out.println("request of select is sending to" +instanceInfo.getHomePageUrl());
                            response = restTemplate.getForEntity(uri.build(), List.class);
                            if(response.getHeaders().get("next").get(0).equalsIgnoreCase("no")){
                                finalNext.add(new AtomicBoolean(false));
                            } else {
                                finalNext.add(new AtomicBoolean(true));
                            }
                            System.out.println("request of select sent to" +instanceInfo.getHomePageUrl());
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        if(!response.getStatusCode().is2xxSuccessful()){
                            throw new RuntimeException(response.toString());
                        }
                        finalResult.addAll(Objects.requireNonNull(response.getBody()));
                        System.out.println("get select data from " +instanceInfo.getHomePageUrl());
                    });
                    futures.add(future);
                }
                nextNumber +=NUMBER_LINES_DATA_TO_SEND;
            } while(next.stream().anyMatch(bool->bool.get()==true));
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            return new ResponseEntity<>(this.limitRows(result, limitRows),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(this.limitRows(result,limitRows),HttpStatus.OK);
    }

    private List limitRows(List result, Integer limit){
        if(limit==null || result == null || result.isEmpty() || result.size()<limit.intValue()) {
            return result;
        }
        return result.subList(0,limit.intValue());
    }

    public ResponseEntity<Integer> countColumn(String tableName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        int result = 0;
        try {
            for( InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + COUNT_COLUMN_PATH + tableName);
                ResponseEntity<Integer> response = restTemplate.getForEntity(uri.build(), Integer.class);
                result = response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new RuntimeException(response.toString());
                }
                System.out.println("request countColumn sent to" + instanceInfo.getHomePageUrl());
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<Float> sumColumn(String tableName, String colName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        float result = 0;
        try {
            for( InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + SUM_COLUMN_PATH + tableName);
                uri.addParameter("colName", String.valueOf(colName));
                ResponseEntity<Float> response = restTemplate.getForEntity(uri.build(), Float.class);
                result += response.getBody();
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new RuntimeException(response.toString());
                }
                System.out.println("request sumColumn sent to" + instanceInfo.getHomePageUrl());
            }

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    public ResponseEntity<Float> avgColumn(String tableName, String colName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        float result = 0;
        int cpt = 0;
        try {
            for( InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + AVG_COLUMN_PATH + tableName);
                uri.addParameter("colName", String.valueOf(colName));
                ResponseEntity<Float> response = restTemplate.getForEntity(uri.build(), Float.class);
                result += response.getBody();
                cpt++;
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new RuntimeException(response.toString());
                }
                System.out.println("request avgColumn sent to" + instanceInfo.getHomePageUrl());
            }

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>((result/cpt), HttpStatus.OK);
    }

    public ResponseEntity<Object> maxColumn(String tableName, String colName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if (microservices == null) {
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        Object resultat = null;
        try {
            Float maxValNum = (float) 0;
            String maxValStr = "";
            boolean firstVal = true;
            for (InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + MAX_COLUMN_PATH + tableName);
                uri.addParameter("colName", String.valueOf(colName));
                ResponseEntity<Object> response = restTemplate.getForEntity(uri.build(), Object.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException(response.toString());
                }

                Object responseBody = response.getBody();

                if (responseBody instanceof Number) {
                    float floatElem = ((Number) responseBody).floatValue();
                    if (firstVal || floatElem > maxValNum) {
                        maxValNum = floatElem;
                        firstVal = false;
                    }
                    resultat = maxValNum;
//                } else if (responseBody instanceof String) {
//                    String strElem = (String) responseBody;
//                    if (firstVal || strElem.compareTo(maxValStr) > 0) {
//                        maxValStr = strElem;
//                        firstVal = false;
//                    }
//                    resultat = maxValStr;
                } else {
                    throw new RuntimeException("Unexpected response body type: " + responseBody.getClass().getSimpleName());
                }
                System.out.println("request maxColumn sent to" +instanceInfo.getHomePageUrl());
            }

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(resultat, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<Object> minColumn(String tableName, String colName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if (microservices == null) {
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        Object resultat = null;
        try {
            Float minValNum = (float) 0;
            String minValStr = "";
            boolean firstVal = true;
            for (InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + MIN_COLUMN_PATH + tableName);
                uri.addParameter("colName", String.valueOf(colName));
                ResponseEntity<Object> response = restTemplate.getForEntity(uri.build(), Object.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException(response.toString());
                }

                Object responseBody = response.getBody();

                if (responseBody instanceof Number) {
                    float floatElem = ((Number) responseBody).floatValue();
                    if (firstVal || floatElem < minValNum) {
                        minValNum = floatElem;
                        firstVal = false;
                    }
                    resultat = minValNum;
//                } else if (responseBody instanceof String) {
//                    String strElem = (String) responseBody;
//                    if (firstVal || strElem.compareTo(minValStr) < 0) {
//                        minValStr = strElem;
//                        firstVal = false;
//                    }
//                    resultat = minValStr;
                } else {
                    throw new RuntimeException("Unexpected response body type: " + responseBody.getClass().getSimpleName());
                }
                System.out.println("request minColumn sent to" +instanceInfo.getHomePageUrl());
            }

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(resultat, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<Boolean> addColumn(String tableName, Column<?> newColumn) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        try {
            for( InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + ADD_COLUMN_PATH + tableName);
                ResponseEntity<Boolean> response = restTemplate.postForEntity(uri.build(), newColumn, Boolean.class);
                if(!response.getStatusCode().is2xxSuccessful()){
                    throw new RuntimeException(response.toString());
                }
                System.out.println("request addColumn sent to" +instanceInfo.getHomePageUrl());
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    public ResponseEntity<Boolean> deleteColumn(String tableName, String colName) {
        List<InstanceInfo> microservices = eurekaClient.getApplication(MICROSERVICE_NAME).getInstances();
        if(microservices==null){
            return new ResponseEntity<>(HttpStatus.RESET_CONTENT);
        }
        try {
            for (InstanceInfo instanceInfo : microservices) {
                String url = instanceInfo.getHomePageUrl();
                RestTemplate restTemplate = new RestTemplate();
                URIBuilder uri = new URIBuilder(url + DEL_COLUMN_PATH + tableName);
                uri.addParameter("colName", String.valueOf(colName));
                URI requestUri = uri.build();
                RequestEntity<?> requestEntity = RequestEntity.delete(requestUri).build();
                ResponseEntity<Boolean> response = restTemplate.exchange(requestEntity, Boolean.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException(response.toString());
                }
                System.out.println("DELETE request sent to: " + instanceInfo.getHomePageUrl());
            }
        } catch (Exception e) {
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

}
