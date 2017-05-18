FROM openjdk:8

ENV USER elite2
ENV GROUP elite2
ENV NAME mobile-web
ENV JAR_PATH mobile-web/build/libs

ARG VERSION

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r -g ${USER} ${GROUP} -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app
COPY run.sh /app

RUN chmod a+x /app/run.sh

EXPOSE 8080

USER elite2

ENTRYPOINT /app/run.sh