#!/usr/bin/env bash
# Generates TSV page tab to add the files to a submission adding the attributes based on a library file
# $1 -> Folder containing the imaging files
# $2 -> Base path for files

find $1 -printf '%P,%f,%y,%s\n' >> files.list;
attrOutput="attrFiles.tsv";
noAttrOutput="noAttrFiles.tsv";
rm -rf ${attrOutput} ${noAttrOutput};

echo -e "Files" >> ${noAttrOutput};
echo -e "Files\tSample\tTimepoint (min)" >> ${attrOutput};

while IFS=',' read -r name file type size
do
  path="$2/$name"
  if [[ "$type" == "f" ]];
  then
    echo "Processing --> $path"
    attrQuery="SELECT
                sample, '|',
                timepoint
              FROM KANDERSON
              WHERE filePath = '$path'";
    fileAttr=$(mysql --user="" --password="" --host="" --port="" --execute="$attrQuery" --skip-column-names --raw --silent database)

    IFS='|' read -ra ADDR <<< "$fileAttr"
    trimmed_sample="$(echo -e "${ADDR[0]}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')";
    trimmed_timepoint="$(echo -e "${ADDR[1]}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')";

    if [[ "$trimmed_sample" == "" ]];
    then
        echo "$path" >> ${noAttrOutput};
    else
        echo -e "$path\t$trimmed_sample\t$trimmed_timepoint" >> ${attrOutput};
    fi
  fi
done < files.list

rm -rf files.list
