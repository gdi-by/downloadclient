#!/bin/bash

#Actually exit on error
set -e

#Assuming we're still in the "misc" dir
cd ..

VERSION=$(xmlstarlet sel -N pom="http://maven.apache.org/POM/4.0.0" -t -v '/pom:project/pom:version' pom.xml)

echo ''
echo 'Building Downloadclient Release ' $VERSION
echo ''
echo ''

rm -r build

echo ''
echo 'Creating Build directories'
mkdir build
mkdir build/config


echo ''
echo 'Copying config from resources to config folder'
cp src/resources/settings.xml build/config
cp src/resources/de/bayern/gdi/model/mimetypes.xml build/config
cp src/resources/de/bayern/gdi/model/verarbeitungsschritte.xml build/config
cp src/resources/de/bayern/gdi/model/proxy.xml.sample build/config

echo ''
echo 'Populating Textfiles'
cp misc/textfiles/* build/
sed -i s/{VERSION}/$VERSION/g build/LIESMICH_Linux.txt
sed -i s/{VERSION}/$VERSION/g build/LIESMICH_Windows.txt


echo ''
echo 'Copying Starter-Scripts'
cp misc/scripts/* build/

echo ''
echo 'Altering the about_*.html to contain the Version ' $VERSION
sed -i s/{project.version}/$VERSION/g src/resources/about/about_*.html


echo ''
echo 'Building Downloadclient Package'
if [ $# -eq 0 ]
  then
    echo '--using NO proxy for mvn tests'
    mvn clean compile package
  else 
    echo '-- using proxy for mvn tests'   
    mvn -Dhttp.proxyHost=$1 -Dhttp.proxyPort=$2 -Dhttps.proxyHost=$1 -Dhttps.proxyPort=$2 clean compile package
fi

#tidy up the version-number alteration above
git checkout -- src/resources/about

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

GISINTERNALS='release-1800-gdal-2-1-0-mapserver-7-0-1.zip'
GISINTERNALSSHA256='d3e2108377113065c8771ccb172d9dd60699fb601dbe79cefda6cc38caa93ed4'
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
echo 'Populating BeTA2007'
cp misc/ntv2/* build/bin/gisinternals/bin/proj/SHARE

echo ''
echo 'Zipping into one file'
pushd build/
zip -pTr downloadclient-$VERSION.zip *
popd

exit 0
