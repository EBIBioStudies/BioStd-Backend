#!/usr/bin/env bash
# Script to calculate the total data size from a CVS generated stats file. The file from the previous month (which is
# expected to be located in the given output path) will be used as base for the current month's results.
#
# Arguments:
# - The path of the stats file to process
# - The path to place the new generated file with the current month size
#
# Output:
# - A file called <YYYYmm>_BIA.txt containing the size of the files for the imaging studies
# - A file called <YYYYmm>_BioStudies.txt containing the size of the files for the non imaging studies
# Note: <YYYYmm> will be replaced with the previous month to the date of the script execution

imagingTotal=0;
regularTotal=0;
while IFS=',' read -r accNo subFileSize filesCount filesSize imaging
do
  files=${subFileSize#*\"};
  files=${files%\"*};
  data=${filesSize#*\"};
  data=${data%\"*};
  images=${imaging#*\"};
  images=${images%\"*};

  if [ ${images} == "true" ]
  then
    imagingTotal=$((imagingTotal+=files+data));
  else
    regularTotal=$((regularTotal+=files+data));
  fi
done < "$1"

currentMonth=$(date --date='-1 month' +%Y%m);
previousMonth=$(date --date='-2 month' +%Y%m);
imagingReportFile="$2/${currentMonth}_BIA.txt";
regularReportFile="$2/${currentMonth}_BioStudies.txt";

cp "$2/${previousMonth}_BIA.txt" ${imagingReportFile};
cp "$2/${previousMonth}_BioStudies.txt" ${regularReportFile};

echo "$currentMonth $imagingTotal" >> "${imagingReportFile}";
echo "$currentMonth $regularTotal" >> "${regularReportFile}";
