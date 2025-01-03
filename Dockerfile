FROM eclipse-temurin:21
COPY ./target/marafone-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]