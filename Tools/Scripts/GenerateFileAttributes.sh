#!/usr/bin/env bash
find $1 -printf '%P,%p,%y,%s\n' >> files.list;
filesOutput="$2.json";
sqlOutput="$2.sql";
rm -rf ${filesOutput} ${sqlOutput};
echo "[" >> ${filesOutput}

while IFS=',' read -r name path type size
do
  currentType="file";
  record="";
  if [ "$type" == "d" ];
  then
    currentType="directory";
    record="{\"path\": \"$path\", \"size\": $size, \"type\": \"$currentType\" },";
  else
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
    fileIdQuery="SELECT fr.id
                FROM FileRef fr, Submission su, Section se
                WHERE fr.sectionId = se.id
                    AND se.submission_id = su.id
                    AND su.accNo = '$3'
                    AND fr.name LIKE '%$name%'";
    # TODO change the credentials to args before commiting
    fileId=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$fileIdQuery" --skip-column-names --silent $4)
    fileAttr=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$attrQuery" --skip-column-names --raw --silent biostd_dev_stats)

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

      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Plate\", \"$trimmedPlate\", 0);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Well\", \"$trimmedWell\", 1);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Replicate\", \"$trimmedReplicate\", 2);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Channel\", \"$trimmedChannel\", 3);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Spot\", \"$trimmedSpot\", 4);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Cell Line Name\", \"$trimmedCellLineName\", 5);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Mutation\", \"$trimmedMutation\", 6);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Compound Id\", \"$trimmedCompoundId\", 7);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES($fileId, \"Compound Name\", \"$trimmedCompoundName\", 8);" >> ${sqlOutput};
    done <<< ${fileAttr}

    record="{
      \"path\": \"$path\",
      \"size\": $size,
      \"attributes\": $attributes,
      \"type\": \"$currentType\" },";
  fi
echo ${record} >> ${filesOutput}
done < files.list

echo "]" >> ${filesOutput}
rm -rf files.list
