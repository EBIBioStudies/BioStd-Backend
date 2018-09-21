#!/usr/bin/env bash
find $1 -printf '%P,%p,%y,%s\n' >> files.list;
filesOutput="$2.json";
sqlOutput="$2.sql";
rm -rf ${filesOutput} ${sqlOutput};
echo "[" >> ${filesOutput}

while IFS=',' read -r name path type size
do
  currentType="file";
  currentRecord="";
  if [ "$type" == "d" ];
  then
    currentType="directory";
    currentRecord="{\"path\": \"$path\", \"size\": $size, \"type\": \"$currentType\" },";
  else
    formattedName=${name// /_};
    plateQuery="SELECT plate FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    wellQuery="SELECT well FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    replicateQuery="SELECT replicate FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    channelQuery="SELECT channel FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    spotQuery="SELECT spot FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    cellQuery="SELECT cellLineName FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    mutationQuery="SELECT mutation FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    compoundIdQuery="SELECT compoundId FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    compoundNameQuery="SELECT compoundName FROM ImagingFilesAttributes WHERE file = '$formattedName'"
    fileIdQuery="SELECT fr.id
                FROM FileRef fr, Submission su, Section se
                WHERE fr.sectionId = se.id
                    AND se.submission_id = su.id
                    AND su.accNo = '$3'
                    AND fr.name LIKE '%$name%'";
    # TODO change the credentials to args before commiting
    fileId=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$fileIdQuery" --skip-column-names --silent $4)
    plate=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$plateQuery" --skip-column-names --silent biostd_dev_stats)
    well=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$wellQuery" --skip-column-names --silent biostd_dev_stats)
    replicate=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$replicateQuery" --skip-column-names --silent biostd_dev_stats)
    channel=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$channelQuery" --skip-column-names --silent biostd_dev_stats)
    spot=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$spotQuery" --skip-column-names --silent biostd_dev_stats)
    cellLineName=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$cellQuery" --skip-column-names --silent biostd_dev_stats)
    mutation=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$mutationQuery" --skip-column-names --silent biostd_dev_stats)
    compoundId=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$compoundIdQuery" --skip-column-names --silent biostd_dev_stats)
    compoundName=$(mysql --user="biostd" --password="biostd" --host="mysql-fg-biostudy.ebi.ac.uk" --port="4469" --execute="$compoundNameQuery" --skip-column-names --silent biostd_dev_stats)

    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Plate', '$plate', 0);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Well', '$well', 1);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Replicate', '$replicate', 2);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Channel', '$channel', 3);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Spot', '$spot', 4);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Cell Line Name', '$cellLineName', 5);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Mutation', '$mutation', 6);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Compound Id', '$compoundId', 7);" >> ${sqlOutput};
    echo "INSERT INTO FileAttribute(fileId, name, value, ord) VALUES('$fileId', 'Compound Name', '$compoundName', 8);" >> ${sqlOutput};

    currentRecord="{
      \"path\": \"$path\",
      \"size\": $size,
      \"attributes\": [
      {\"name\": \"Plate\", \"value\": \"$plate\"},
      {\"name\": \"Well\", \"value\": \"$well\"},
      {\"name\": \"Replicate\", \"value\": \"$replicate\"},
      {\"name\": \"Channel\", \"value\": \"$channel\"},
      {\"name\": \"Spot\", \"value\": \"$spot\"},
      {\"name\": \"Cell Line Name\", \"value\": \"$cellLineName\"},
      {\"name\": \"Mutation\", \"value\": \"$mutation\"},
      {\"name\": \"Compound Id\", \"value\": \"$compoundId\"},
      {\"name\": \"Compound Name\", \"value\": \"$compountName\"}],
      \"type\": \"$currentType\" },";
  fi
echo ${currentRecord} >> ${filesOutput}
done < files.list

echo "]" >> ${filesOutput}
rm -rf files.list
