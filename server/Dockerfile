# Build stage
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true -Xmx512m"

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests -o

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
    libicu78 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/logs /app/.config /app/data

COPY --from=build /app/target/server.jar ./target/server.jar

ENV JAVA_OPTS="--enable-native-access=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/server.jar"]