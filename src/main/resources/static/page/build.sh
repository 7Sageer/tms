#!/bin/bash

echo "build start"

DEST=/Users/xiweicheng/tms/src/main/resources/static
SRC=/Users/xiweicheng/tms-landing

echo "DEST DIR: $DEST"
echo "SRC DIR: $SRC"

echo "au pkg --env prod"

cd $SRC
au pkg --env prod

echo "rm static/scripts & static/index.html"

rm -rf $DEST/scripts
rm -rf $DEST/index.html

echo "cp tms-landing to tms"

cp -rf $SRC/scripts $DEST
cp -rf $SRC/index.html $DEST

echo "build end"