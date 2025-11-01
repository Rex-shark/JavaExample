# File: Dockerfile
# 先建置 frontend
FROM node:22-alpine AS frontend-builder
WORKDIR /frontend
COPY vue_demo1/package*.json ./
RUN npm ci --silent
COPY vue_demo1 ./
RUN npm run build

# 再用 Maven 建置後端，並把前端 dist 放入 Spring Boot 的 resources
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
# 將前端建置結果放到後端的 static 目錄，供 Spring Boot 打包
COPY --from=frontend-builder /frontend/dist /app/DockerDemo/src/main/resources/static
RUN mvn -f DockerDemo/pom.xml clean package -DskipTests

# 最後以小型 JRE image 執行 jar
FROM amazoncorretto:17
WORKDIR /app
ARG JAR_FILE=DockerDemo/target/DockerDemo-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]