# Generate FTP links for every released submission
# $1 Database user
# $2 Database password
# $3 Database host
# $4 Database port
# $4 Database name
# $6 BioStudies host
# $7 BioStudies port
# Example: ./GenerateFtpLinksFromDB.sh biostd_dev biostd_dev mysql-fg-biostudy-dev.ebi.ac.uk 4639 biostd_dev biostudy-bia.ebi.ac.uk 8788

echo "Generating FTP links..."
query="SELECT relPath FROM Submission su, Section se WHERE se.id = su.rootSection_id AND se.type <> 'Project' AND released = true AND version > 0"
releasedSubmissions=$(mysql --user="$1" --password="$2" --host="$3" --port="$4" --execute="$query" --raw --silent $5)

while read -r relPath
do
  echo "Processing $relPath"

  response=$(curl -X POST -F "relPath=$relPath" http://$6:$7/submissions/ftp/generate)

  if [ "$response" == "" ]
  then
    echo -e "Success\n"
  else
    echo -e "Error: $response\n"
  fi
done <<< "${releasedSubmissions}"
