# MQTT Spring Boot Demo

## 项目简介

硬件设备的Spring Boot + MQTT 3.1集成应用demo，使用Spring Boot 3.2.0 + MQTT v3客户端，实现MQTT 3.1.1协议。

## 技术栈

- Java
- Spring Boot 3.2.0
- MQTT 3.1.1 协议
- Maven

## 功能特性

- MQTT 3.1.1协议支持
- 硬件设备集成
- Spring Boot框架集成
- 消息发布/订阅功能

## 安装说明

`ash
# 克隆项目
git clone https://github.com/YaBoom/mqtt-springboot-demo.git

# 使用Maven构建
cd mqtt-springboot-demo
mvn clean install

# 运行应用
mvn spring-boot:run
`

## 配置说明

在 pplication.properties 或 pplication.yml 中配置MQTT连接参数：

`properties
# MQTT Broker配置
mqtt.broker=tcp://localhost:1883
mqtt.client.id=your-client-id
mqtt.username=username
mqtt.password=password
`

## 使用方法

1. 启动MQTT Broker
2. 配置项目中的MQTT连接参数
3. 启动Spring Boot应用
4. 应用将自动连接到MQTT Broker并开始收发消息

## 项目结构

`
mqtt-springboot-demo/
├── src/main/java/     # 源代码
├── src/main/resources/ # 配置文件
│   └── application.properties # 应用配置
├── pom.xml            # Maven配置
└── README.md          # 项目说明文件
`

## API文档

- 消息发布接口
- 消息订阅接口
- 连接管理接口

## 贡献指南

欢迎提交Issue和Pull Request！

## 许可证

本项目采用 [MIT License](LICENSE) 许可证。
