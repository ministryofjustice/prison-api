#!/bin/sh
exec java ${JAVA_OPTS} \
  -XX:+PrintCompilation -XX:TieredStopAtLevel=4 -XX:Tier3CompileThreshold=1 -XX:Tier4CompileThreshold=1 \
  -XX:Tier3InvocationThreshold=1 -XX:Tier4InvocationThreshold=1 \
  -XX:Tier3MinInvocationThreshold=1 -XX:Tier4MinInvocationThreshold=1 \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  -Dcom.sun.management.jmxremote.local.only=false \
  -Djava.security.egd=file:/dev/./urandom \
  -Doracle.jdbc.J2EE13Compliant=true \
  -Dcom.zaxxer.hikari.housekeeping.periodMs=${CONNECTION_POOL_STATS_PERIOD:-360000} \
  -javaagent:/app/agent.jar \
  -jar /app/app.jar
