FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY app.jar /app.jar
RUN test -f /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=prod"]
