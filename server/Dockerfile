# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradlew.bat .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle gradle.properties* ./

RUN chmod +x gradlew

# Cache dependencies (resolve only, don't compile yet)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies || true

COPY src ./src

# Build jar + copy runtime deps - cache gradle home AND build output dir, daemon enabled
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    --mount=type=cache,target=/app/build,sharing=locked \
    ./gradlew jar copyDeps --build-cache && \
    cp /app/build/libs/server.jar /app/server.jar && \
    rm -rf /app/lib && cp -r /app/build/libs/lib /app/lib

# Runtime stage
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN mkdir -p /app/logs /app/.config /app/data

COPY --from=build /app/server.jar ./server.jar
COPY --from=build /app/lib ./lib

ENV JAVA_OPTS="--enable-native-access=ALL-UNNAMED"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp 'server.jar:lib/*' com.osuserverlist.bjar.App"]