package com.yuncheng.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerApplication {

    private static final Logger log = LoggerFactory.getLogger(McpServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
        log.info("===============McpServerApplication服务启动成功");
    }

    @Bean
    public ToolCallbackProvider mathTools(MathService mathService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mathService).build();
    }
}