package com.example.eurekaClient.controller;

import com.example.eurekaClient.db.Database;
import com.example.eurekaClient.service.RequestService;
import com.example.eurekaClient.service.MicroserviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest
@Import({MicroserviceService.class, RequestService.class, Database.class})
class ApplicationRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void createTable() throws Exception {

        final String content = "{\"name\":\"tableCreated\",\"columns\":[{\"columnName\":\"Sexe\",\"type\":\"STRING\"},{\"columnName\":\"Prénom\",\"type\":\"STRING\"},{\"columnName\":\"Année de naissance\",\"type\":\"STRING\"},{\"columnName\":\"age\",\"type\":\"SHORT\"}],\"storage\":{}}";
        this.mvc.perform(MockMvcRequestBuilders.post("/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("tableCreated"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns.length()").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[0].columnName").value("Sexe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[0].type").value("STRING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[1].columnName").value("Prénom"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[1].type").value("STRING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[2].columnName").value("Année de naissance"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[2].type").value("STRING"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[3].columnName").value("age"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.columns[3].type").value("SHORT"));
    }

    @Test
    void insertDataFromCsvFile() throws Exception {
        final String createTable = "{\"name\":\"table\",\"columns\":[{\"columnName\":\"Sexe\",\"type\":\"STRING\"},{\"columnName\":\"Prénom\",\"type\":\"STRING\"},{\"columnName\":\"Année de naissance\",\"type\":\"STRING\"},{\"columnName\":\"age\",\"type\":\"INT\"}],\"storage\":{}}";

        this.mvc.perform(MockMvcRequestBuilders.post("/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createTable));

        String content = "[\"F,Béatrice,1964,60\",\"F,Charlotte,1988,50\"]";
        this.mvc.perform(MockMvcRequestBuilders.post("/insert/table")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
}