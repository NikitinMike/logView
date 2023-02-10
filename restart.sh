#!/bin/sh
#### ./gradlew bootJar

date
mv ~/workspace/log/log.jar ~/workspace/log/`date +%F-%T`.log.jar

cp build/libs/*.jar ~/workspace/log/log.jar
sudo service log restart
echo LOGVIEW RESTARTED
