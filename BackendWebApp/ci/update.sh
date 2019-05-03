#!/bin/bash

#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   1: deploy configuration base path path.
#   2: application port
#   3: deploy jar artifact name, i.e. biostudy-20190430.jar
#
# Example execution
#
#  ./update.sh /ebi/teams/biostudies/backend/apps/webapp 8586 biostudy-20190430.jar
#
cd $(dirname $0)

# kill current application in specified port
PID=$(netstat -antp 2>/dev/null -tlnp | awk '/:'"$2"' */ {split($NF,a,"/"); print a[1]}')

# Wait kill to finish
while $(kill -9 ${PID} 2>/dev/null); do sleep 1;done;

## set specific application environment properties
source $1/environment.sh

## deploy new version of application
nohup $JAVA_HOME/bin/java \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n \
    -jar "$1/$3" \
    --spring.config.location="$1/application.yml" \
    --server.port=$2 >> logs.txt &
