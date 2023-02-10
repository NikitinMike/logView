#!/bin/sh

date +%Y.%m.%d

#wget https://github.com/P3TERX/GeoLite.mmdb/releases/download/2023.02.07/GeoLite2-City.mmdb
mv GeoLite2-City.mmdb GeoLite2-City.mmdb.`date +%F-%T`
wget https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb

mv ~/workspace/log/GeoLite2-City.mmdb ~/workspace/log/GeoLite2-City.mmdb.`date +%F-%T`

cp GeoLite2-City.mmdb ~/workspace/log/GeoLite2-City.mmdb

sudo service log restart
echo LOGVIEW RESTARTED
