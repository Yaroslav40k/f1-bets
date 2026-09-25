# syntax=docker/dockerfile:1
# Build context is the repository root: the modules inherit from the root POM, so that POM
# (and its module list) must be present for Maven to resolve the reactor.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependency downloads separately from source changes. All module POMs are copied
# because the root POM declares them as modules and Maven validates the whole reactor.
COPY pom.xml .
COPY event-outcome-producer/pom.xml event-outcome-producer/
COPY event-matcher-worker/pom.xml event-matcher-worker/
COPY bet-settler-worker/pom.xml bet-settler-worker/
RUN mvn -q -B -pl event-outcome-producer -am dependency:go-offline

COPY event-outcome-producer/src event-outcome-producer/src
RUN mvn -q -B -pl event-outcome-producer -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/event-outcome-producer/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
