# download-client
GDI-BY DownloadClient

A Java 8 / JavaFX based desktop client to download geo data.
This project is in a very experimental stage.  
Do not use it by now! :-)

## Build

    $ mvn clean compile

## Bundle

    $ mvn clean package

## Run
To start with UI:

    $ mvn exec:java

To start in headless mode:

    $ mvn exec:java -Dexec.args=-headless 

If you have built the single assembly version:

    $ java -jar target/downloadclient-1.0-SNAPSHOT.jar

## License

This is Free Software covered by the terms of the Apache License 2.0.  
See LICENSE file for details.
