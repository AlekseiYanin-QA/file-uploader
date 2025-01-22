package com.edu.fileupload.controller;

import com.edu.fileupload.dto.FileUploadResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class FileUploadControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/file/upload";
    }

    @Test
    void testUploadTxtFiles() throws Exception {
        // Подготовка тестовых файлов
        List<ClassPathResource> files = IntStream.rangeClosed(1, 3)
                .mapToObj(i -> new ClassPathResource("test-files/file" + i + ".txt"))
                .collect(Collectors.toList());

        // Создание запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        files.forEach(file -> body.add("files", file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправка запроса
        ResponseEntity<FileUploadResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                FileUploadResponse.class
        );

        // Проверка ответа
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(3, response.getBody().getUploadedFiles().size());
    }

    @Test
    void testUploadNonTxtFiles() throws Exception {
        // Подготовка тестовых файлов (не .txt)
        ClassPathResource nonTxtFile = new ClassPathResource("test-files/file11.xlsx");

        // Создание запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", nonTxtFile);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправка запроса
        ResponseEntity<FileUploadResponse> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                requestEntity,
                FileUploadResponse.class
        );

        // Проверка ответа
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Only .txt files are allowed", response.getBody().getMessage());
    }
}