#!/usr/bin/env bash
RESPONSE=$(curl -s \
  --header "Content-Type: application/json" \
  --request POST \
  --data '{"login":"admin_user@ebi.ac.uk", "password":"123456"}' \
  http://localhost:8586/biostd/auth/signin);

TOKEN=$(echo $RESPONSE | python -c 'import sys, json; print json.load(sys.stdin)["sessid"]');

echo "Session Key: $TOKEN";
