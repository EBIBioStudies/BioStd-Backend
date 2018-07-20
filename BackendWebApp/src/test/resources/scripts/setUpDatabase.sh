docker build -t biostudies-mysql ../;
docker run -d \
  --name biostudies-mysql01 \
  -e MYSQL_ROOT_PASSWORD=admin \
  -e MYSQL_DATABASE=BioStudiesDev \
  -p 3306:3306 \
  biostudies-mysql;
