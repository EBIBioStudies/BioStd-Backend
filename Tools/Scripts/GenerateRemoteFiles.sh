# $1 EBI user
# $2 server: i.e biostudy-prod.ebi.ac.uk
# $3 submission files folder
# $4 local output folder

atouch() {
  mkdir -p $(sed 's/\(.*\)\/.*/\1/' <<< $1) && touch $1
}

ssh $1@$2 find $3 -type f | while read line; do
  echo "Processing $line"
  path=${line##*/Files/}
  echo "$path"
  atouch $4/$path
done
