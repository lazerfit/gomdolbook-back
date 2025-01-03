package com.gomdolbook.api.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gomdolbook.api.dto.AladinAPI;
import com.gomdolbook.api.dto.AladinAPI.Item;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class BookControllerTest {

    static MockWebServer server;
    static RestTemplateBuilder restTemplateBuilder;

    @Autowired
    BookController bookController;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        restTemplateBuilder = new RestTemplateBuilder();
        server = new MockWebServer();
        server.start();

    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.aladin", () -> String.format("http://localhost:%s/", server.getPort()));
    }

    @Test
    void findBook() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilder.rootUri(server.url("/").toString()).build();

        String response = objectMapper.writeValueAsString(new AladinAPI(1, 1, 1,
            List.of(new Item("소년이 온다", "한강", "2014-05-19", "2024 노벨문학상",
                "9788936434120", "image1", "노벨문학상",
                "창비"))));

        server.enqueue(
            new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(response)
        );

        ResponseEntity<String> entity = restTemplate.getForEntity("/api/v1/book/9788936434120",
            String.class);

        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getBody()).contains("소년이 온다");
    }
}
