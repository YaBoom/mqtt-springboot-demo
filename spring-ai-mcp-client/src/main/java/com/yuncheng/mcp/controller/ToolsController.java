package com.yuncheng.mcp.controller;

import com.yuncheng.mcp.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @className: ToolsController
 * @Description: TODO
 * @author: zhuyt
 * @date: 25/9/29 9:53
 */
@RestController
@RequestMapping("/tools")
public class ToolsController {

    private final ChatClient chatClient ;

    @Resource
    private AgentService agentService;

    public ToolsController(ChatClient.Builder aiClientBuilder, ToolCallbackProvider mcpTools) {
//        Map commonHeaders = new HashMap();
//        OpenAiChatOptions options = OpenAiChatOptions.builder().httpHeaders(commonHeaders).build();

        this.chatClient = aiClientBuilder
                .defaultTools(mcpTools)
               // .defaultOptions(options)
                .build() ;
    }

    @GetMapping("/daxue")
    public ResponseEntity<String> getCurrentDaxue(String prompt) {
        System.err.println(prompt) ;
        String response = this.chatClient
                .prompt(prompt)
                .call().content() ;
        return ResponseEntity.ok(response) ;
    }

    @GetMapping("/ip")
    public ResponseEntity<String> getIpAddressInfo(String prompt) {
        System.err.println(prompt) ;
        String response = this.chatClient
                .prompt(prompt)
                .call().content() ;
        return ResponseEntity.ok(response) ;
    }

    @GetMapping("/getDressingAdvice/{userInput}")
    public String getDressingAdvice(@PathVariable String userInput) {
        return agentService.getDressingAdvice(userInput);
    }
}
