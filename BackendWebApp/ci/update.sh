#!/bin/bash

#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   1: deployment environment (dev|beta|prod).
#   2: deploy configuration base path path.
#   3: application port
#   4: deploy jar artifact name, i.e. biostudy-20190430.jar
#
cd $(dirname $0)

## kill current application
PID=$(cat $2/pid.txt)
while $(fuser -k $3/tcp>/dev/null); do sleep 1;done;

## set specific application environment properties
source environment.sh

## deploy new version of application
nohup $JAVA_HOME/bin/java \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n \
    -jar "$2/$4.jar" \
    --spring.config.location="$2/application.yml" \
    --server.port=$3 >> logs.txt &
