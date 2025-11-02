package com.yuncheng.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @className: WeatherConfig
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/8/25 9:15
 */
@Configuration
public class WeatherConfig {
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        restTemplate.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return  restTemplate;
    }
}
