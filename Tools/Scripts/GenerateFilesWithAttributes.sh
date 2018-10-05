#!/usr/bin/env bash
# Generates the attributes and SQL commands to insert the files for a submission adding the attributes based on a library file
# $1 -> Folder containing the imaging files
# $2 -> Study root section id
# $3 -> Initial file id
# $4 -> Base path for files
find $1 -printf '%P,%f,%y,%s\n' >> files.list;
filesOutput="output.json";
sqlOutput="output.sql";
rm -rf ${filesOutput} ${sqlOutput};

echo 'SET FOREIGN_KEY_CHECKS=0;' >> ${sqlOutput};
echo "[" >> ${filesOutput}

fileId=$(($3+1000));
ord=0;

while IFS=',' read -r name file type size
do
  record="";
  if [ "$type" == "f" ];
  then
    attrQuery="SELECT
                plate, '|',
                well, '|',
                replicate, '|',
                channel, '|',
                spot, '|',
                cellLineName, '|',
                mutation, '|',
                compoundId, '|',
                compoundName
              FROM ImagingFilesAttributes
              WHERE file = '${name// /_}'";
    fileAttr=$(mysql --user="" --password="" --host="" --port="" --execute="$attrQuery" --skip-column-names --raw --silent database)

    while IFS="|" read -r plate well replicate channel spot cellLineName mutation compoundId compoundName;
    do
      trimmedPlate=${plate//[[:space:]]/};
      trimmedWell=${well//[[:space:]]/};
      trimmedReplicate=${replicate//[[:space:]]/};
      trimmedChannel=${channel//[[:space:]]/};
      trimmedSpot=${spot//[[:space:]]/};
      trimmedCellLineName=${cellLineName//[[:space:]]/};
      trimmedMutation=${mutation//[[:space:]]/};
      trimmedCompoundId=${compoundId//[[:space:]]/};
      trimmedCompoundName=${compoundName//[[:space:]]/};

      attributes="[
        {\"name\": \"Plate\", \"value\": \"$trimmedPlate\"},
        {\"name\": \"Well\", \"value\": \"$trimmedWell\"},
        {\"name\": \"Replicate\", \"value\": \"$trimmedReplicate\"},
        {\"name\": \"Channel\", \"value\": \"$trimmedChannel\"},
        {\"name\": \"Spot\", \"value\": \"$trimmedSpot\"},
        {\"name\": \"Cell Line Name\", \"value\": \"$trimmedCellLineName\"},
        {\"name\": \"Mutation\", \"value\": \"$trimmedMutation\"},
        {\"name\": \"Compound Id\", \"value\": \"$trimmedCompoundId\"},
        {\"name\": \"Compound Name\", \"value\": \"$trimmedCompoundName\"}]";

      echo "INSERT INTO FileRef(id, directory, name, size, tableIndex, sectionId, ord, path) VALUES($fileId, 0, '$4/$name', $size, $ord, $2, $ord, 'u/$4/$name');" >> ${sqlOutput}
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Plate\", \"$trimmedPlate\", 0, 0, 0);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Well\", \"$trimmedWell\", 0, 0, 1);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Replicate\", \"$trimmedReplicate\", 0, 0, 2);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Channel\", \"$trimmedChannel\", 0, 0, 3);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Spot\", \"$trimmedSpot\", 0, 0, 4);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Cell Line Name\", \"$trimmedCellLineName\", 0, 0, 5);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Mutation\", \"$trimmedMutation\", 0, 0, 6);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Compound Id\", \"$trimmedCompoundId\", 0, 0, 7);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Compound Name\", \"$trimmedCompoundName\", 0, 0, 8);" >> ${sqlOutput};
    done <<< ${fileAttr}

    record="{
      \"path\": \"$4/$name\",
      \"size\": $size,
      \"attributes\": $attributes,
      \"type\": \"file\" },";

    echo ${record} >> ${filesOutput}
    ((fileId++));
    ((ord++));
  fi
done < files.list

echo 'SET FOREIGN_KEY_CHECKS=1;' >> ${sqlOutput};
echo "]" >> ${filesOutput}
rm -rf files.list
