#!/usr/bin/env bash
cd $(dirname $0)
export JAVA_HOME=/nfs/ma/home/java/jdk8

nohup $JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8001,suspend=n -jar ../biostudy.jar \
    --spring.config.location="/ebi/teams/biostudies/backend/apps/webapp/default.yml,/ebi/teams/biostudies/backend/apps/webapp/dev.yml"  >> ../logs.txt &

PID=$!
echo $PID > ../pid.txt
