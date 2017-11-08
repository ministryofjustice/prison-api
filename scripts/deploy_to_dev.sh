#!/bin/bash

VERSION=latest
if [ "$#" -gt 0 ]; then
  VERSION=$1
fi

echo Deploying version $VERSION to kiosk...

TDIR=$(mktemp -d)
trap "{ cd - ; rm -rf ${TDIR}; exit 255; }" SIGINT
mkdir -p ${TDIR}/${VERSION}/env

echo Building script file...
cat << EOF > ${TDIR}/${VERSION}/run.sh
#!/bin/bash

docker stop elite2-api
docker rm -vf elite2-api
docker pull sysconjusticesystems/elite2-api:$VERSION

docker run -d --name elite2-api -h elite2-api \
    --restart=always \
    --env-file ./env/config_nomis_dev.env \
    sysconjusticesystems/elite2-api:$VERSION
exit 0
EOF
chmod +x ${TDIR}/${VERSION}/run.sh

cp config_nomis_dev.env ${TDIR}/${VERSION}/env
cd ${TDIR}

# ssh on to box
echo copying file to remote server...
scp ${TDIR}/${VERSION}/run.sh ci_agent@10.200.1.152:.
scp -r ${TDIR}/${VERSION}/env ci_agent@10.200.1.152:.
echo Running deploy script
ssh ci_agent@10.200.1.152 "./run.sh"

echo Cleaning up...
rm -rf ${TDIR}
exit 0
