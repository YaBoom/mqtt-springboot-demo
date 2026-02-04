package com.yuncheng.mcp;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    @Resource
    private RestTemplate restTemplate;

    @Resource
    private Environment env;

    @Tool(description = "根据城市名称获取天气预报")
    public String getWeatherByCity(String city) {
        log.info("===============getWeatherByCity方法被调用：city="+city);
        Map<String, String> mockData = Map.of(
                "西安", "天气炎热",
                "北京", "晴空万里",
                "上海", "阴雨绵绵"
        );
        return mockData.getOrDefault(city, "抱歉：未查询到对应城市！");
    }

    @Tool(description = "根据城市名获取天气信息")
    public String getWeather(String cityName, String date) {
        return cityName + date + "的天气是18℃-25℃ 小雨。";
    }

    @Tool(description = "根据天气信息获取穿衣建议")
    public String getDressingAdvice(String weatherInfo) {
        // 模拟根据天气信息获取穿衣建议
        return "薄外套 + 防水鞋";
    }

//    @Tool(description = "根据城市名称获取天气预报通过第三方API")
//    public String getWeatherByCityThirdApi(String city) {
//        log.info("===============getWeatherByCityThirdApi方法被调用：city="+city);
//        String apiUrl = env.getProperty("third.weather.url");
//        apiUrl = String.format(apiUrl,city);
//        ResponseEntity<String> forEntity = restTemplate.getForEntity(apiUrl, String.class);
//
//        if (200 == forEntity.getStatusCode().value()){
//            System.out.println(forEntity.getBody());
//        }else {
//            return mockData.getOrDefault(city, "抱歉：未查询到对应城市！");
//        }
//
//
//    }

}
