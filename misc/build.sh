#!/bin/bash
echo 'Building Downloadclient Release' $1
echo ''
echo ''

rm -r build

echo ''
echo 'Creating Build directories'
mkdir build
mkdir build/config

VERSION=$(xmlstarlet sel -N pom="http://maven.apache.org/POM/4.0.0" -t -v '/pom:project/pom:version' pom.xml)

echo ''
echo 'Copying config from resources to config folder'
cp src/resources/serviceSetting.xml build/config
cp src/resources/de/bayern/gdi/model/mimetypes.xml build/config
cp src/resources/de/bayern/gdi/model/verarbeitungsschritte.xml build/config

echo ''
echo 'Populating Textfiles'
cp misc/textfiles/* build/
sed -i s/{VERSION}/$VERSION/g build/LIESMICH.txt

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

GISINTERNALS='release-1800-gdal-mapserver.zip'
GISINTERNALSSHA256='ea43d6d3219e9b512913b07ac05b75e2619b328290b67b00b16ee09ec161d118'
wget http://download.gisinternals.com/sdk/downloads/$GISINTERNALS

echo 'Testing if SHA256 sums are equal...'
TEST=`sha256sum $GISINTERNALS | grep $GISINTERNALSSHA256`
if [ -z "$TEST" ]; then
    echo 'sha256 sum of gisinternals did not match... exiting!'
    exit 1
fi

unzip $GISINTERNALS -d gisinternals
rm $GISINTERNALS

popd

echo ''
echo 'Zipping into one file'
pushd build/
zip -pTr downloadclient-$VERSION.zip *
popd

exit 0
