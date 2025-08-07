FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/chat-backend-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]