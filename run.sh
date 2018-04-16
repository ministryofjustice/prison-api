#!/bin/sh
exec java ${JAVA_OPTS} \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Djava.security.egd=file:/dev/./urandom \
  -Doracle.jdbc.J2EE13Compliant=true \
  -jar /app/app.jar