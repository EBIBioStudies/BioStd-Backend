#!/usr/bin/env bash

find $1 -printf '%p,%y,%s\n' >> files.list;
outputFile="$2.sql"
ord=0;
rm -rf ${outputFile};

while IFS=',' read -r path type size
do
  currentType="0"
  if [ "$type" == "d" ];
  then currentType="1"
  fi
  echo "INSERT INTO FileRef(directory, name, size, tableIndex, sectionId, ord, path) VALUES($currentType, '$path', $size, $ord, $3, $ord, '$path');" >> ${outputFile}
  ((ord++));
done < files.list

rm -rf files.list
