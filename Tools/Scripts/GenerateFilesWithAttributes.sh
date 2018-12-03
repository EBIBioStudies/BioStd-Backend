#!/usr/bin/env bash
# Generates the attributes and SQL commands to insert the files for a submission adding the attributes based on a library file
# $1 -> Folder containing the imaging files
# $2 -> Study root section id
# $3 -> Initial file id
# $4 -> Base path for files
find $1 -printf '%P,%f,%y,%s\n' >> files.list;
filesOutput="output.json";
sqlOutput="output.sql";
tsvOutput="output.tsv";
rm -rf ${filesOutput} ${sqlOutput} ${tsvOutput};

echo 'SET FOREIGN_KEY_CHECKS=0;' >> ${sqlOutput};
echo "[" >> ${filesOutput}
echo -e "Files\tNaCl Concentration\tTime (minutes)" >> ${tsvOutput}

fileId=$(($3+1000));
ord=0;

while IFS=',' read -r name file type size
do
  record="";
  tsvRecord="";
  if [[ "$type" == "f" ]];
  then
    attrQuery="SELECT
                nacl, '|',
                osmotic
              FROM IDR0047
              WHERE file = '${name// /_}'";
    fileAttr=$(mysql --user="" --password="" --host="" --port="" --execute="$attrQuery" --skip-column-names --raw --silent database)

    while IFS="|" read -r nacl osmotic;
    do
      trimmedNacl=${nacl//[[:space:]]/};
      trimmedOsmotic=${osmotic//[[:space:]]/};

      attributes="[
        {\"name\": \"NaCl Concentration\", \"value\": \"$trimmedNacl\"},
        {\"name\": \"Time (minutes)\", \"value\": \"$trimmedOsmotic\"}]";

      echo "INSERT INTO FileRef(id, directory, name, size, tableIndex, sectionId, ord, path) VALUES($fileId, 0, '$4/$name', $size, $ord, $2, $ord, 'u/$4/$name');" >> ${sqlOutput}
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"NaCl Concentration\", \"$trimmedNacl\", 0, 0, 0);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Time (minutes) After Osmotic Stress\", \"$trimmedOsmotic\", 0, 0, 1);" >> ${sqlOutput};
    done <<< ${fileAttr}

    record="{
      \"path\": \"$4/$name\",
      \"size\": $size,
      \"attributes\": $attributes,
      \"type\": \"file\" },";

    echo -e "$4/$name\t$trimmedNacl\t$trimmedOsmotic" >> ${tsvOutput};
    echo ${record} >> ${filesOutput}

    ((fileId++));
    ((ord++));
  fi
done < files.list

echo 'SET FOREIGN_KEY_CHECKS=1;' >> ${sqlOutput};
echo "]" >> ${filesOutput}
rm -rf files.list
