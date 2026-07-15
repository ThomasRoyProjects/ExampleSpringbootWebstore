FROM eclipse-temurin:17-jdk-jammy@sha256:723151f3fc88ca2060153ee08ab8dbbea7983d6ed6f2622fe440acf178737c94 AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew

COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:17-jre-jammy@sha256:475d8e96b4b2bfe08999e5e854755c773af1581acdf959a4545d88f0696a2339
RUN groupadd --system webstore \
    && useradd --system --gid webstore --home-dir /app --shell /usr/sbin/nologin webstore

WORKDIR /app
COPY --from=build --chown=webstore:webstore /workspace/build/libs/webstore-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads && chown webstore:webstore /app/uploads

USER webstore
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=6 \
  CMD curl --fail --silent --show-error http://127.0.0.1:8080/actuator/health > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
