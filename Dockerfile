# 构建阶段
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制Maven配置文件
COPY pom.xml .
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 复制构建好的JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有者
RUN chown spring:spring app.jar

# 切换到非root用户
USER spring:spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 运行应用
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "app.jar"]