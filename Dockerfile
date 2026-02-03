FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Копируем уже собранный jar
COPY target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]