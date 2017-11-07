#! /bin/bash

# DEPLOY MODIFIED IMAGE TO DOCKER HUB
SHA1=$1
ENVIRONMENT=$2
mv .deploy/Dockerfile Dockerfile
mv .deploy/startgrassroot.sh.$ENVIRONMENT startgrassroot.sh
mv .deploy/build-jar.sh build-jar.sh
chmod +x build-jar.sh
chmod +x startgrassroot.sh
docker build --rm=false -t awsassembly/grassroot:$ENVIRONMENT$SHA1 .
docker login -e $DOCKER_EMAIL -u $DOCKER_USER -p $DOCKER_PASS
docker push awsassembly/grassroot:$ENVIRONMENT$SHA1
