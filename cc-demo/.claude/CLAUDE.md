# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Test (all)
./mvnw clean test

# Run single test class
./mvnw test -Dtest=CcDemoApplicationTests

# Build OCI container image
./mvnw build-image
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Architecture

This is a minimal Spring Boot 3.5 application (Java 21) serving as a Claude Code demo project.

- **Entry point:** `src/main/java/com/rex/ccdemo/CcDemoApplication.java`
- **Root package:** `com.rex.ccdemo`
- **Config:** `src/main/resources/application.yaml` (currently only sets `spring.application.name=cc-demo`)
- **Lombok** is configured as an annotation processor — use it freely for boilerplate reduction.

## 語言規則

所有回覆、思考與計劃制定一律使用**繁體中文（台灣用語）**。

## Workspace

This project lives inside a larger multi-module workspace (`C:\Users\rexre\IdeaProjects\JavaExample\`) alongside other demo projects (AiAgentDemo, AiOpenAiDemo, LinebotDemo, DockerDemo, etc.), but is built independently via its own Maven wrapper.