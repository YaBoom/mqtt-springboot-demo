# spring_ai_mcp

#### 介绍
该demo主要演示基于Spring AI如何实现MCP开发和调用。首先自定义了两个MCP Server，其中：一个是算术计算器MCP Server，并通过sdtio传输协议发布，另一个是天气预报MCP Server，通过sse传输协议发布。然后实现一个MCP Client，并调用通过qwen-plus大模型来完成整个 MCP 调用流程。

#### 软件架构
	大模型采用阿里云千问qwen-plus大模型。
	基于sse协议实现一个MCP Server，模拟开发一个天气预报服务，通过sse传输协议发布为MCP Server。
	基于stdio的MCP服务端通过标准输入输出流与客户端通信，适用于作为子进程被客户端启动和管理的场景：模拟开发一个算术计算器服务，通过stdio传输协议发布为MCP Server。 
	基于 Spring AI 的异步 MCP 客户端，调用SSE 和 Stdio 两种MCP服务。server1采用SSE方式，连接指向http://localhost:9090，server2采用Stdio 方式，Stdio 通过 Java 命令启动，指定 Jar 文件位置。
