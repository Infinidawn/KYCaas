FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ARG JAR_FILE=target/gateway-service-0.1.0.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","/app/app.jar"]

