#!/usr/bin/env bash
# Script to calculate the total data size from a CVS generated stats file. The file from the previous month (which is
# expected to be located in the given output path) will be used as base for the current month's results.
# Arguments:
# - The path of the stats file to process
# - The path to put the new generated file with the current month size
# - The asset name
# Output: A file containing the results for the current month based on the given stats file

total=0;
while IFS=',' read -r title filesSize filesCount dataSize
do
  files=${filesSize#*\"};
  files=${files%\"*};
  data=${dataSize#*\"};
  data=${data%\"*};
  total=$((total+=files+data));
done < "$1"

currentMonth=$(date +'%Y%m');
previousMonth=$(date +%Y%m -d "`date +%d` day ago");
newFile="$2/${currentMonth}_$3.txt";

cp "$2/${previousMonth}_$3.txt" ${newFile};
echo "$currentMonth $total" >> "${newFile}";
