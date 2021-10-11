FROM openjdk:16-slim AS builder

WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

FROM openjdk:16-slim

RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

ENV TZ=Europe/London
ENV SPRING_DATASOURCE_PASSWORD=r9KDvw37g13
ENV SPRING_REPLICA_DATASOURCE_PASSWORD=r9KDvw37g13
ENV SPRING_REPLICA_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521:CNOMT3
ENV SPRING_REPLICA_DATASOURCE_USERNAME=api_proxy_user_ro
ENV SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=2
ENV SPRING_REPLICA_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
ENV SERVER_PORT=8080
ENV SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://sign-in-dev.hmpps.service.justice.gov.uk/auth/.well-known/jwks.json

RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/prison-api*.jar /app/app.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/run.sh /app
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.dev.json /app

USER 2000

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
