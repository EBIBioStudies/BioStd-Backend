#!/usr/bin/env bash
# Generates the JSON attributes and SQL commands to insert the files for a submission
# $1 -> Folder containing the submission files
# $2 -> Study root section id
# $3 -> Base path for submission files
find $1 -printf '%P,%f,%y,%s\n' >> files.list;
filesOutput="output.json";
sqlOutput="output.sql";
rm -rf ${filesOutput} ${sqlOutput};

echo "[" >> ${filesOutput}
ord=0;

while IFS=',' read -r name file type size
do
  record="";
  if [[ "$type" == "f" ]];
  then
    echo "Processing $name"
    echo "INSERT INTO FileRef(directory, name, size, tableIndex, sectionId, ord, path) VALUES(0, '$3/$name', $size, $ord, $2, $ord, 'u/$3/$name');" >> ${sqlOutput}
    echo "{ \"path\": \"$3/$name\", \"size\": $size, \"type\": \"file\" }," >> ${filesOutput}

    ((ord++));
  fi
done < files.list

echo "]" >> ${filesOutput}
rm -rf files.list
