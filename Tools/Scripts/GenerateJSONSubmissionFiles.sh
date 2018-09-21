#!/usr/bin/env bash

find $1 -printf '%f,%p,%y,%s\n' >> files.list;
outputFile="$2.json";
rm -rf ${outputFile};

while IFS=',' read -r name path type size
do
  currentType="file"
  if [ "$type" == "d" ];
  then currentType="directory"
  fi
  echo "{\"name\": \"$name\", \"path\": \"$path\", \"size\": $size, \"type\": \"$currentType\" }," >> ${outputFile}
done < files.list

rm -rf files.list
