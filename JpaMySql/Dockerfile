FROM amazoncorretto:17

# 可透過 --build-arg JAR_FILE=target/YourJarName.jar 來覆蓋
ARG JAR_FILE=target/JpaMySql-0.0.1-SNAPSHOT.jar
WORKDIR /app
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
