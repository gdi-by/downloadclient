#!/bin/bash

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
        if [[ -n "$JAVAFX_HOME" ]]; then
            echo "Starting GDI-BY Downloadclient using Java version 11"
            echo "Using JAVAFX in JAVAFX_HOME=$JAVAFX_HOME"
            $_java --module-path $JAVAFX_HOME/lib --add-modules=javafx.base,javafx.controls,javafx.fxml,javafx.graphics,javafx.web --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=javafx.graphics/javafx.application=ALL-UNNAMED --add-opens=javafx.graphics/javafx.geometry=ALL-UNNAMED --add-opens=javafx.web/javafx.scene.web=ALL-UNNAMED --add-opens=javafx.web/com.sun.webkit=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.util=ALL-UNNAMED --add-exports javafx.base/com.sun.javafx.logging=ALL-UNNAMED --add-exports javafx.graphics/com.sun.prism=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.ui=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED --add-exports javafx.graphics/com.sun.glass.utils=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.font=ALL-UNNAMED --add-exports javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED --add-exports javafx.graphics/com.sun.prism.paint=ALL-UNNAMED --add-exports javafx.graphics/com.sun.scenario.effect=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.text=ALL-UNNAMED --add-exports javafx.graphics/com.sun.javafx.iio=ALL-UNNAMED -jar downloadclient.jar --config=config
        else
            echo "JAVAFX_HOME is not set. Please read the documentation for further information."
        fi
    else
        echo "No suitable Java version found. Please read the documentation for further information."
    fi
fi
