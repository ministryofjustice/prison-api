FROM openjdk:8

ENV USER noms
ENV GROUP noms
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

EXPOSE 7080

USER noms

ENTRYPOINT /app/run.sh
