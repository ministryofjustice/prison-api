#!/usr/bin/env bash

docker stop $(docker ps -a | grep "elite2-api" | awk '{print $1}')
docker rm -vf $(docker ps -a | grep "elite2-api" | awk '{print $1}')


docker run -d --name elite2-api -h elite2-api \
   --restart=always \
   -p 7080:8080 \
   --env-file ./config_nomis_dev.env \
   sysconjusticesystems/elite2-api:latest