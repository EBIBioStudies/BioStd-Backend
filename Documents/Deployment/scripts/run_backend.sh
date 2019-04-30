#!/usr/bin/env bash
#
# Set java home and execute spring boot jar application.
#
# Parameters:
#   1: deployment environment (dev|beta|prod).
#   2: deploy configuration base path path.
#

cd $(dirname $0)
export JAVA_HOME=/nfs/ma/home/java/jdk8

nohup $JAVA_HOME/bin/java \
    -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n \
    -jar "$2/biostudy.jar" \
    --spring.config.location="$2/default.yml,$2/$1.yml" >> ../logs.txt &

PID=$!
echo $PID > ../pid.txt
