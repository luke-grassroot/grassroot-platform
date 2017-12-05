#!/bin/bash

# SET ENVIRONMENT VARIABLES BASED ON THE ENVIRONMENT PREVIOUSLY SETUP VIA
. /usr/src/grassroot/environment/environment-variables

# START GRASSROOT
cd /usr/src/grassroot/

if [[ ! -f .clean ]]; then
  #if .clean file exist, removes it, so gradlew will run without "clean".
  echo "startgrassroot using quick build, via gradle bootRun"
  echo "Saving tmp files to /usr/src/grassroot/.gradle/tmp"
  ./gradlew bootRun -Dspring.profiles.active=$PROFILE,fast -g /usr/src/grassroot/.gradle/tmp --configure-on-demand --parallel --daemon
else
  echo "startgrassroot using clean build"
  echo "Saving tmp files to /usr/src/grassroot/.gradle/tmp"
  ./gradlew clean build -x test -g /usr/src/grassroot/.gradle/tmp --configure-on-demand --parallel --daemon
  # ./scripts/startmqtt.sh
  java -Djava.security.egd=file:/dev/urandom -Dspring.profiles.active=$PROFILE,fast -jar -Xmx1024m grassroot-webapp/build/libs/grassroot-webapp-1.1.1.1a.jar > log/grassroot-app.log 2>&1 &
fi
