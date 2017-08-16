#!/bin/sh
NAME=${NAME:-mobile-web}

JAR=$(find . -name ${NAME}*.jar|head -1)
java ${JAVA_OPTS} -Doracle.jdbc.J2EE13Compliant=true -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"

