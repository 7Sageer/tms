#!/bin/bash

echo "deploy to tomcat start"

DEST=/Users/xiweicheng/Applications/apache-tomcat-8.5.59
SRC=/Users/xiweicheng/tms-landing

echo "DEST DIR: $DEST"
echo "SRC DIR: $SRC"

echo "au pkg --env prod"

cd $SRC
au pkg --env prod

DT=`date +"%Y%m%d%H%M%S"`
mkdir -p $DEST/backup/tms-landing/$DT
cp -rf $DEST/webapps/ROOT/WEB-INF/classes/static/scripts $DEST/backup/tms-landing/$DT
cp -rf $DEST/webapps/ROOT/WEB-INF/classes/static/index.html $DEST/backup/tms-landing/$DT
echo "backup path: $DEST/backup/tms/tms-landing/$DT"

echo "rm static/scripts & static/index.html"

rm -rf $DEST/webapps/ROOT/WEB-INF/classes/static/scripts
rm -rf $DEST/webapps/ROOT/WEB-INF/classes/static/index.html

echo "cp tms-landing to local tomcat"

cp -rf $SRC/scripts $DEST/webapps/ROOT/WEB-INF/classes/static
cp -rf $SRC/index.html $DEST/webapps/ROOT/WEB-INF/classes/static

sh $DEST/bin/shutdown.sh
sleep 5
sh $DEST/bin/startup.sh

echo "deploy to tomcat end"