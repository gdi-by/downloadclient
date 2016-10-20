#!/bin/bash
echo 'Building Downloadclient Release' $1
echo ''
echo ''

rm -r build

echo ''
echo 'Creating Build directories'
mkdir build
mkdir build/config

echo ''
echo 'Copying config from resources to config folder'
cp src/resources/serviceSetting.xml build/config
cp src/resources/de/bayern/gdi/model/mimetypes.xml build/config
cp src/resources/de/bayern/gdi/model/verarbeitungsschritte.xml build/config

echo ''
echo 'Populating Textfiles'
cp misc/textfiles/* build/
sed -i s/{VERSION}/$1/g build/LIESMICH.txt

echo ''
echo 'Copying Starter-Scripts'
cp misc/scripts/* build/


echo ''
echo 'Building Downloadclient Package'
mvn clean compile package
cp target/downloadclient-*.jar build/downloadclient.jar
chmod u+x build/downloadclient.jar


echo ''
echo 'Downloading Stuff which is required for windows...'
mkdir build/bin
pushd build/bin/

echo ''
echo '**************************************************'
echo '*WARNING DONWLOADING FROM UNSAFE EXTERNAL SOURCE!*'
echo '**************************************************'
echo ''

wget http://download.gisinternals.com/sdk/downloads/release-1800-gdal-mapserver.zip
unzip release-1800-gdal-mapserver.zip -d gisinternals
popd

echo ''
echo 'Zipping into one file'
pushd build/
zip -pTr downloadclient-$1.zip *
popd

exit 0
