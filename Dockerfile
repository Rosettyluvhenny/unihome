FROM maven:3.9-amazoncorretto-21-alpine AS build
WORKDIR /app

# Alpine: cần ca-certificates để Maven tải deps HTTPS ổn định
RUN apk add --no-cache ca-certificates

# Cache Maven deps
COPY pom.xml .
RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -q -DskipTests dependency:go-offline

# Copy source
COPY src ./src

RUN --mount=type=cache,id=maven-cache,target=/root/.m2 \
    mvn -B -DskipTests package


# RUNTIME STAGE

FROM build

RUN apk add --no-cache tzdata \
 && cp /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime \
 && echo "Asia/Ho_Chi_Minh" > /etc/timezone

# Non-root user
RUN addgroup -S appuser && adduser -S appuser -G appuser
USER appuser

WORKDIR /app

# Copy jar
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms128m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
