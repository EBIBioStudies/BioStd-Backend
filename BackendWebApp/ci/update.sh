#!/bin/bash

#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   1: deploy configuration base path path.
#   2: application port
#   3: deploy jar artifact name, i.e. biostudy-20190430.jar
#
cd $(dirname $0)

netstat -nap | grep 8586


## kill current application
#PID=$(netstat -antp 2>/dev/null -tlnp | awk '/:8586 */ {split($NF,a,"/"); print a[1]}')
#while $(kill -9 ${PID} 2>/dev/null); do sleep 1;done;

## set specific application environment properties
source environment.sh

## deploy new version of application
nohup $JAVA_HOME/bin/java \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n \
    -jar "$1/$3" \
    --spring.config.location="$1/application.yml" \
    --server.port=$2 >> logs.txt &
