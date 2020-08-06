#!/bin/sh

if type -p java; then
    echo "Found 'java' executable in PATH=$PATH"
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "Found 'java' executable in JAVA_HOME=$JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "No Java Runtime (JRE/JDK) installed! Please read the documentation for further information."
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo Java version="$version"
    if [[ "$version" = "1.8"* ]]; then
        echo "Starting GDI-BY Downloadclient using Java version 1.8"
        $_java -jar downloadclient.jar --config=config
    elif [[ "$version" = "11.0"* ]]; then
        echo "Starting GDI-BY Downloadclient using Java version 11"
        $_java -jar downloadclient.jar --config=config
    else
        echo "No suitable Java version found. Please read the documentation for further information."
    fi
fi
