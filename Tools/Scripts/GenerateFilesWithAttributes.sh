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
echo -e "Plate\tReplicate\tWell\tTime Point\tField\tWell Number\tCharacteristics (Organism)\tCharacteristics (Strain)\tGene Identifier\tGene Symbol\tChannels\tMean Growth Rate (/h)\tStdev Growth Rate (/h)\tNoise(Stdev/Mean)" >> ${tsvOutput}

fileId=$(($3+1000));
ord=0;

while IFS=',' read -r name file type size
do
  record="";
  tsvRecord="";
  currentTimePoint=$(echo "$name" | cut -d '/' -f 2);
  currentPlate=$(echo "$file" | cut -d '-' -f 1);
  rest=$(echo "$file" | cut -d '-' -f 2);
  currentReplicate=$(echo "$rest" | cut -d '_' -f 1);
  currentWell=$(echo "$rest" | cut -d '_' -f 2);
  currentField=$(echo $(echo "$rest" | cut -d '_' -f 3) | cut -d '.' -f 1);

  if [[ "$type" == "f" ]] && [[ "$file" != ".DS_Store" ]];
  then
    echo "processing --> plate: $currentPlate -- replicate: $currentReplicate -- well: $currentWell -- field: $currentField -- timepoint: $currentTimePoint";
    attrQuery="SELECT
                wellNumber, '|',
                orgChar, '|',
                strainChar, '|',
                geneId, '|',
                geneSymbol, '|',
                channels, '|',
                meanGrowthRate, '|',
                stGrowthRate, '|',
                noise
              FROM RD
              WHERE plate = '$currentPlate' AND replicate='$currentReplicate' AND well='$currentWell'";
    fileAttr=$(mysql --user="root" --password="admin" --host="172.22.68.131" --port="3306" --execute="$attrQuery" --skip-column-names --raw --silent LibraryFiles)

    while IFS="|" read -r wellNumber orgchar strainChar geneId geneSymbol channels meanGrowthRate stGrowthRate noise;
    do
      trimmed_wellNumber=${wellNumber//[[:space:]]/};
      trimmed_orgChar=${orgChar//[[:space:]]/};
      trimmed_strainChar=${strainChar//[[:space:]]/};
      trimmed_geneId=${geneId//[[:space:]]/};
      trimmed_geneSymbol=${geneSymbol//[[:space:]]/};
      trimmed_channels=${channels//[[:space:]]/};
      trimmed_meanGrowthRate=${meanGrowthRate//[[:space:]]/};
      trimmed_stGrowthRate=${stGrowthRate//[[:space:]]/};
      trimmed_noise=${noise//[[:space:]]/};

      attributes="[
        {\"name\": \"Plate\", \"value\": \"$currentPlate\"},
        {\"name\": \"Replicate\", \"value\": \"$currentReplicate\"},
        {\"name\": \"Well\", \"value\": \"$currentWell\"},
        {\"name\": \"Time Point\", \"value\": \"$currentTimePoint\"},
        {\"name\": \"Field\", \"value\": \"$currentField\"},
        {\"name\": \"Well Number\", \"value\": \"$trimmed_wellNumber\"},
        {\"name\": \"Characteristics (Organism)\", \"value\": \"$trimmed_orgChar\"},
        {\"name\": \"Characteristics (Strain)\", \"value\": \"$trimmed_strainChar\"},
        {\"name\": \"Gene Identifier\", \"value\": \"$trimmed_geneId\"},
        {\"name\": \"Gene Symbol\", \"value\": \"$trimmed_geneSymbol\"},
        {\"name\": \"Channels\", \"value\": \"$trimmed_channels\"},
        {\"name\": \"Mean Growth Rate (/h)\", \"value\": \"$trimmed_meanGrowthRate\"},
        {\"name\": \"Stdev Growth Rate (/h)\", \"value\": \"$trimmed_stGrowthRate\"},
        {\"name\": \"Noise\", \"value\": \"$trimmed_noise\"}]";

      echo "INSERT INTO FileRef(id, directory, name, size, tableIndex, sectionId, ord, path) VALUES($fileId, 0, '$4/$name', $size, $ord, $2, $ord, 'u/$4/$name');" >> ${sqlOutput}
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Plate\", \"$currentPlate\", 0, 0, 0);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Replicate\", \"$currentReplicate\", 0, 0, 1);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Well\", \"$currentWell\", 0, 0, 2);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Time Point\", \"$currentTimePoint\", 0, 0, 3);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Field\", \"$currentField\", 0, 0, 4);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Well Number\", \"$trimmed_wellNumber\", 0, 0, 5);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Characteristics (Organism)\", \"$trimmed_orgChar\", 0, 0, 6);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Characteristics (Strain)\", \"$trimmed_strainChar\", 0, 0, 7);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Gene Identifier\", \"$trimmed_geneId\", 0, 0, 8);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Gene Symbol\", \"$trimmed_geneSymboln\", 0, 0, 9);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Channels\", \"$trimmed_channels\", 0, 0, 10);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Mean Growth Rate (/h)\", \"$trimmed_meanGrowthRate\", 0, 0, 11);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Stdev Growth Rate (/h)\", \"$trimmed_stGrowthRate\", 0, 0, 12);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Noise\", \"$trimmed_noise\", 0, 0, 13);" >> ${sqlOutput};
    done <<< ${fileAttr}

    record="{
      \"path\": \"$4/$name\",
      \"size\": $size,
      \"attributes\": $attributes,
      \"type\": \"file\" },";

    echo -e "$4/$name\t$currentPlate\t$currentReplicate\t$currentWell\t$currentTimePoint\t$currentField\t$trimmed_wellNumber\t$trimmed_orgChar\t$trimmed_strainChar\t$trimmed_geneId\t$trimmed_geneSymbol\t$trimmed_channels\t$trimmed_meanGrowthRate\t$trimmed_stGrowthRate\t$trimmed_noise" >> ${tsvOutput};
    echo ${record} >> ${filesOutput}

    ((fileId++));
    ((ord++));
  fi
done < files.list

echo 'SET FOREIGN_KEY_CHECKS=1;' >> ${sqlOutput};
echo "]" >> ${filesOutput}
rm -rf files.list
