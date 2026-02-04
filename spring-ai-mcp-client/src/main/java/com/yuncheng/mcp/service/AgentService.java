package com.yuncheng.mcp.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @className: AgentService
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/10/4 18:11
 */
@Service
public class AgentService {
    private final ChatClient chatClient;

    public AgentService(ChatClient.Builder chatClientBuilder, ToolCallbackProvider tools) {
        String systemPrompt = """
                你作为穿衣助手Agent，请严格按以下步骤为用户推荐穿衣搭配：
                ### 步骤1：获取当前日期
                当前日期为{currentDate}。
                
                ### 步骤2：校验用户输入
                如果用户输入不包含城市名则提示用户'请输入城市名', 如果用户输入不包含日期则提示用户'请指定日期'。
                
                ### 步骤3：根据城市名和日期获取天气信息
                调用MCP Server工具'根据城市名获取天气信息'获取天气信息，入参：城市名cityName、日期date(yyyy-mm-dd格式)从用户输入中提取。
                
                ### 步骤4：根据天气信息获取穿衣建议
                调用MCP Server工具'根据天气信息获取穿衣建议'获取穿衣建议并输出，入参：weatherInfo，从上一步获取的天气信息的输出赋值weatherInfo。
                
                """;
        String currentDataStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        systemPrompt = systemPrompt.replace("{currentDate}", currentDataStr);
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).defaultTools(tools).build();
    }

    public String getDressingAdvice(String userInput) {
        // 调用模型生成
        return chatClient.prompt().user(userInput).call().content();
    }
}
