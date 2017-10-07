FROM openjdk:8-jdk-alpine
RUN apk update && apk upgrade && apk add --update curl && rm -rf /var/cache/apk/*

ENV NAME mobile-web
ENV JAR_PATH mobile-web/build/libs

ARG VERSION

WORKDIR /app

COPY ${JAR_PATH}/${NAME}*.jar /app
COPY run.sh /app

RUN chmod a+x /app/run.sh

EXPOSE 8080

ENV TZ=Europe/London
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT /app/run.sh