package com.yuncheng.mcp.config;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class WeatherConfigTest {

    @Resource
    private RestTemplate restTemplate;

    @Test
    public void test30() throws IOException {
        String apiURL ="https://api.weather.com/data?city=广州";
        String response = restTemplate.getForObject(apiURL, String.class,"广州");
    }
  
}