#!/bin/sh
FILE=$1
shift
java -jar downloadclient.jar -headless $FILE --config=config $@
