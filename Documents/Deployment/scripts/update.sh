#!/bin/bash

cd $(dirname $0)
PID=$(cat ../pid.txt)
while $(kill -9 $PID 2>/dev/null); do sleep 1;done;
sh run_backend.sh
