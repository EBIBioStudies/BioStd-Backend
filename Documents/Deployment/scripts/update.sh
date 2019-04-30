#!/bin/bash
#
# Kill current deployed instance and start deployment script.
#
# Parameters:
#   1: deployment environment (dev|beta|prod).
#   2: deploy configuration base path path.
#

cd $(dirname $0)
PID=$(cat $2/pid.txt)
while $(kill -9 $PID 2>/dev/null); do sleep 1;done;
sh run_backend.sh $1 $2
