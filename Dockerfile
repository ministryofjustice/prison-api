FROM openjdk:8-jdk-alpine
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

RUN apk update \
  && apk upgrade \
  && apk add netcat-openbsd \
  && apk add --update curl \
  && rm -rf /var/cache/apk/*
docker
WORKDIR /app

COPY mobile-web/build/libs/mobile-web*.jar /app/app.jar
COPY run.sh /app

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

ENTRYPOINT ["/bin/sh", "/app/run.sh"]