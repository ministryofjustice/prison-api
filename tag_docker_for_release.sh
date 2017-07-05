#!/bin/bash


display_usage() {
	echo -e "\nUsage:\ntag_docker_for_release <VERSION> <RELEASE>\n"
}

# if less than two arguments supplied, display usage
if [  $# -ne 2 ]
then
    display_usage
    exit 1
fi

VERSION=$1
RELEASE=$2

docker login
docker pull sysconjusticesystems/elite2-api:$VERSION
docker tag sysconjusticesystems/elite2-api:$VERSION sysconjusticesystems/elite2-api:$RELEASE
docker push sysconjusticesystems/elite2-api:$RELEASE