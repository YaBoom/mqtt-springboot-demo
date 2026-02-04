package com.yuncheng.mcp;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.tool.ToolCallbackProvider;

import java.util.Scanner;

@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpClientApplication.class, args);
    }

    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder,
                                                 ToolCallbackProvider tools,
                                                 ConfigurableApplicationContext context) {
        return args -> {
            // 构建ChatClient并注入MCP工具
            var chatClient = chatClientBuilder
                    .defaultTools(tools)
                    .build();

            // 创建Scanner对象用于接收用户输入
            Scanner scanner = new Scanner(System.in);

            System.out.println(">>> 欢迎使用问答系统！输入'exit'退出程序。");

            while (true) {
                // 提示用户输入问题
                System.out.print("\n>>> QUESTION: ");
                String userInput = scanner.nextLine();

                // 如果用户输入"exit"，则退出循环
                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println(">>> 已退出问答系统。");
                    break;
                }

                // 使用ChatClient与LLM交互
                try {
                    System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());
                } catch (Exception e) {
                    System.out.println("\n>>> ERROR: 无法处理您的请求，请稍后再试。");
                    e.printStackTrace();
                }
            }

            // 关闭Spring上下文
            context.close();
            scanner.close();
        };
    }
}