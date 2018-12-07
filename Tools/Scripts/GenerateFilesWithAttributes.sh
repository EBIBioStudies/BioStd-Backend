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
                plate, '|',
                row, '|',
                col, '|',
                well, '|',
                ch4, '|',
                geneTT, '|',
                cellArea, '|',
                nucleusArea, '|',
                nucleusCellArea, '|',
                neighborFraction, '|',
                localCellDensity, '|',
                nuclearRoundness, '|',
                doughnutNuclei, '|',
                totalYT, '|',
                ratioYTMouse, '|',
                ratioYTRabitt, '|',
                cellNumber, '|',
                normCA, '|',
                normNA, '|',
                normNucA, '|',
                normNucR, '|',
                normNF, '|',
                normLCD, '|',
                normTotalYTMouse, '|',
                obsPredYTMouse, '|',
                YTRatioPredMouse, '|',
                obsPredYTRabitt, '|',
                YTRatioPredRabitt
              FROM SERO
              WHERE file = '${name// /_}'";
    fileAttr=$(mysql --user="" --password="" --host="" --port="" --execute="$attrQuery" --skip-column-names --raw --silent database)

    while IFS="|" read -r plate filePrefix row col well ch4 geneTT cellArea nucleusArea nucleusCellArea neighborFraction localCellDensity nuclearRoundness doughnutNuclei totalYT ratioYTMouse ratioYTRabitt cellNumber normCA normNA normNucA normNucR normNF normLCD normTotalYTMouse obsPredYTMouse YTRatioPredMouse obsPredYTRabitt YTRatioPredRabitt;
    do
      trimmed_plate=${plate//[[:space:]]/};
      trimmed_row=${row//[[:space:]]/};
      trimmed_col=${col//[[:space:]]/};
      trimmed_well=${well//[[:space:]]/};
      trimmed_ch4=${ch4//[[:space:]]/};
      trimmed_geneTT=${geneTT//[[:space:]]/};
      trimmed_cellArea=${cellArea//[[:space:]]/};
      trimmed_nucleusArea=${nucleusArea//[[:space:]]/};
      trimmed_nucleusCellArea=${nucleusCellArea//[[:space:]]/};
      trimmed_neighborFraction=${neighborFraction//[[:space:]]/};
      trimmed_localCellDensity=${localCellDensity//[[:space:]]/};
      trimmed_nuclearRoundness=${nuclearRoundness//[[:space:]]/};
      trimmed_doughnutNuclei=${doughnutNuclei//[[:space:]]/};
      trimmed_totalYT=${totalYT//[[:space:]]/};
      trimmed_ratioYTMouse=${ratioYTMouse//[[:space:]]/};
      trimmed_ratioYTRabitt=${ratioYTRabitt//[[:space:]]/};
      trimmed_cellNumber=${cellNumber//[[:space:]]/};
      trimmed_normCA=${normCA//[[:space:]]/};
      trimmed_normNA=${normNA//[[:space:]]/};
      trimmed_normNucA=${normNucA//[[:space:]]/};
      trimmed_normNucR=${normNucR//[[:space:]]/};
      trimmed_normNF=${normNF//[[:space:]]/};
      trimmed_normLCD=${normLCD//[[:space:]]/};
      trimmed_normTotalYTMouse=${normTotalYTMouse//[[:space:]]/};
      trimmed_obsPredYTMouse=${obsPredYTMouse//[[:space:]]/};
      trimmed_YTRatioPredMouse=${YTRatioPredMouse//[[:space:]]/};
      trimmed_obsPredYTRabitt=${obsPredYTRabitt//[[:space:]]/};
      trimmed_YTRatioPredRabitt=${YTRatioPredRabitt//[[:space:]]/};

      attributes="[
        {\"name\": \"Plate\", \"value\": \"$trimmed_plate\"},
        {\"name\": \"Row\", \"value\": \"$trimmed_row\"},
        {\"name\": \"Col\", \"value\": \"$trimmed_col\"},
        {\"name\": \"Well Name\", \"value\": \"$trimmed_well\"},
        {\"name\": \"CH4\", \"value\": \"$trimmed_ch4\"},
        {\"name\": \"Gene Target/Treatment\", \"value\": \"$trimmed_geneTT\"},
        {\"name\": \"Cell Area\", \"value\": \"$trimmed_cellArea\"},
        {\"name\": \"Nucleus Area\", \"value\": \"$trimmed_nucleusArea\"},
        {\"name\": \"Nucleus/Cell Area\", \"value\": \"$trimmed_nucleusCellArea\"},
        {\"name\": \"Neighbor Fraction\", \"value\": \"$trimmed_neighborFraction\"},
        {\"name\": \"Local Cell Density\", \"value\": \"$trimmed_localCellDensity\"},
        {\"name\": \"Nuclear Roundness\", \"value\": \"$trimmed_nuclearRoundness\"},
        {\"name\": \"Doughnut Nuclei\", \"value\": \"$trimmed_doughnutNuclei\"},
        {\"name\": \"Total YAP/TAZ\", \"value\": \"$trimmed_totalYT\"},
        {\"name\": \"YAP/TAZ ratio (mouse Santa Cruz)\", \"value\": \"$trimmed_ratioYTMouse\"},
        {\"name\": \"YAP/TAZ ratio (rabbit Novus)\", \"value\": \"$trimmed_ratioYTRabitt\"},
        {\"name\": \"Cell Number\", \"value\": \"$trimmed_cellNumber\"},
        {\"name\": \"Normalized Cell Area\", \"value\": \"$trimmed_normCA\"},
        {\"name\": \"Normalized Nuclear Area\", \"value\": \"$trimmed_normNA\"},
        {\"name\": \"Normalized Nucleus Area/Cell Area\", \"value\": \"$trimmed_normNucA\"},
        {\"name\": \"Normalized Nuclear Roundness\", \"value\": \"$trimmed_normNucR\"},
        {\"name\": \"Normalized Neighbor Fraction\", \"value\": \"$trimmed_normNF\"},
        {\"name\": \"Normalized Local Cell Density\", \"value\": \"$trimmed_normLCD\"},
        {\"name\": \"Normalized Total YAP/TAZ (mouse Santa Cruz)\", \"value\": \"$trimmed_normTotalYTMouse\"},
        {\"name\": \"Observed-Predicted YAP/TAZ ratio (mouse Santa Cruz)\", \"value\": \"$trimmed_obsPredYTMouse\"},
        {\"name\": \"YAP/TAZ ratio prediction error (mouse Santa Cruz)\", \"value\": \"$trimmed_YTRatioPredMouse\"},
        {\"name\": \"Observed-Predicted YAP/TAZ ratio (rabbit Novus)\", \"value\": \"$trimmed_obsPredYTRabitt\"},
        {\"name\": \"YAP/TAZ ratio prediction error (rabbit Novus)\", \"value\": \"$trimmed_YTRatioPredRabitt\"}]";

      echo "INSERT INTO FileRef(id, directory, name, size, tableIndex, sectionId, ord, path) VALUES($fileId, 0, '$4/$name', $size, $ord, $2, $ord, 'u/$4/$name');" >> ${sqlOutput}
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Plate\", \"$trimmed_plate\", 0, 0, 0);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Row\", \"$trimmed_row\", 0, 0, 1);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Col\", \"$trimmed_col\", 0, 0, 2);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Well Name\", \"$trimmed_well\", 0, 0, 3);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"CH4\", \"$trimmed_ch4\", 0, 0, 4);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Gene Target/Treatment\", \"$trimmed_geneTT\", 0, 0, 5);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Cell Area\", \"$trimmed_cellArea\", 0, 0, 6);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Nucleus Area\", \"$trimmed_nucleusArea\", 0, 0, 7);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Nucleus/Cell Area\", \"$trimmed_nucleusCellArea\", 0, 0, 8);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Neighbor Fraction\", \"$trimmed_neighborFraction\", 0, 0, 9);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Local Cell Density\", \"$trimmed_localCellDensity\", 0, 0, 10);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Nuclear Roundness\", \"$trimmed_nuclearRoundness\", 0, 0, 11);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Doughnut Nuclei\", \"$trimmed_doughnutNuclei\", 0, 0, 12);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Total YAP/TAZ\", \"$trimmed_totalYT\", 0, 0, 13);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"YAP/TAZ ratio (mouse Santa Cruz)\", \"$trimmed_ratioYTMouse\", 0, 0, 14);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"YAP/TAZ ratio (rabbit Novus)\", \"$trimmed_ratioYTRabitt\", 0, 0, 15);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Cell Number\", \"$trimmed_cellNumber\", 0, 0, 16);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Cell Area\", \"$trimmed_normCA\", 0, 0, 17);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Nuclear Area\", \"$trimmed_normNA\", 0, 0, 18);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Nucleus Area/Cell Area\", \"$trimmed_normNucA\", 0, 0, 19);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Nuclear Roundness\", \"$trimmed_normNucR\", 0, 0, 20);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Neighbor Fraction\", \"$trimmed_normNF\", 0, 0, 21);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Local Cell Density\", \"$trimmed_normLCD\", 0, 0, 22);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Normalized Total YAP/TAZ (mouse Santa Cruz)\", \"$trimmed_normTotalYTMouse\", 0, 0, 23);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Observed-Predicted YAP/TAZ ratio (mouse Santa Cruz)\", \"$trimmed_obsPredYTMouse\", 0, 0, 24);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"YAP/TAZ ratio prediction error (mouse Santa Cruz)\", \"$trimmed_YTRatioPredMouse\", 0, 0, 25);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"Observed-Predicted YAP/TAZ ratio (rabbit Novus)\", \"$trimmed_obsPredYTRabitt\", 0, 0, 26);" >> ${sqlOutput};
      echo "INSERT INTO FileAttribute(file_id, name, value, numValue, reference, ord) VALUES($fileId, \"YAP/TAZ ratio prediction error (rabbit Novus)\", \"$trimmed_YTRatioPredRabitt\", 0, 0, 27);" >> ${sqlOutput};
    done <<< ${fileAttr}

    record="{
      \"path\": \"$4/$name\",
      \"size\": $size,
      \"attributes\": $attributes,
      \"type\": \"file\" },";

    echo -e "$4/$name\t$trimmed_plate\t$trimmed_row\t$trimmed_col\t$trimmed_well\t$trimmed_ch4\t$trimmed_geneTT\t$trimmed_cellArea\t$trimmed_nucleusArea\t$trimmed_nucleusCellArea\t$trimmed_neighborFraction\t$trimmed_localCellDensity\t$trimmed_nuclearRoundness\t$trimmed_doughnutNuclei\t$trimmed_totalYT\t$trimmed_ratioYTMouse\t$trimmed_ratioYTRabitt\t$trimmed_cellNumber\t$trimmed_normCA\t$trimmed_normNucA\t$trimmed_normNucRt\t$trimmed_normNF\t$trimmed_normLCD\t$trimmed_normTotalYTMouse\t$trimmed_obsPredYTMouse\t$trimmed_YTRatioPredMouse\t$trimmed_obsPredYTRabitt\t$trimmed_YTRatioPredRabitt" >> ${tsvOutput};
    echo ${record} >> ${filesOutput}

    ((fileId++));
    ((ord++));
  fi
done < files.list

echo 'SET FOREIGN_KEY_CHECKS=1;' >> ${sqlOutput};
echo "]" >> ${filesOutput}
rm -rf files.list
