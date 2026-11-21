# build stage, dependencies are cached in their own layer so code changes rebuild fast
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# runtime stage, only the jre and the jar
FROM eclipse-temurin:17-jre
WORKDIR /app

# run as a normal user, never as root
RUN useradd --system --uid 1001 blog \
    && mkdir -p /app/images \
    && chown -R blog /app
USER blog

COPY --from=build /build/target/app-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=9090
EXPOSE 9090

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
