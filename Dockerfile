FROM openjdk:8-slim
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY build/libs/elite2-api*.jar /app/app.jar
COPY run.sh /app

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

ENTRYPOINT ["/bin/sh", "/app/run.sh"]