FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app

ENV MAVEN_OPTS="-Djava.net.preferIPv4Stack=true -Xmx512m"

# Cache dependencies and plugins (only re-runs if pom.xml changes)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build JAR (only re-runs if source changes)
COPY src ./src
RUN mvn -B package -DskipTests -o

# Runtime dirs
RUN mkdir -p /app/logs /app/.config /app/data

ENTRYPOINT ["java", "-jar", "target/server.jar"]