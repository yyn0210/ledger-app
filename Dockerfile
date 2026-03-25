<<<<<<< HEAD
# 多阶段构建 - Spring Boot 应用
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 pom.xml 并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
=======
# 多阶段构建 Dockerfile for Ledger App Backend

# 构建阶段
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .

# 下载依赖
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 创建非 root 用户
<<<<<<< HEAD
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 复制构建的 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R appuser:appgroup /app
=======
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# 从构建阶段复制 jar 包
COPY --from=builder /app/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appgroup /app

# 切换到非 root 用户
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
USER appuser

# 暴露端口
EXPOSE 8080

<<<<<<< HEAD
=======
# JVM 参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs"

>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# 启动应用
<<<<<<< HEAD
ENTRYPOINT ["java", "-jar", "app.jar"]
=======
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
>>>>>>> 8b276bd7cad2de2730fddd7f4684cd33bf31cfe1
