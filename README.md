# GDI-BY DownloadClient 
[![Build Status](https://travis-ci.org/gdi-by/downloadclient.svg?branch=master)][travis]
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)][license]

[travis]:  https://travis-ci.org/gdi-by/downloadclient
[license]: https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)


Der Download-Client ist eine Desktop-Anwendung zum einfachen Herunterladen von Geodaten, die über Downloaddienste verfügbar sind. Für die heruntergeladenen Geodaten können optional Weiterverarbeitungsschritte (z. B. Formatkonvertierung) definiert und ausgeführt werden. Die Konfiguration der Download- und Weiterverarbeitungsschritte kann darüber hinaus abgespeichert und über ein Konsolenprogramm erneut ausgeführt werden.

## Systemvoraussetzungen

Für die Ausführung des Download-Clients wird mindestens Java 1.8.0.40 und JavaFX benötigt.

Aktuelle Java-Versionen können hier heruntergeladen werden: http://www.oracle.com/technetwork/java/javase/downloads/index.html


## Dokumentation

Eine Anwender-Dokumentation steht hier bereit: http://downloadclient-gdi-by.readthedocs.io/de/docs/index.html


## Hinweise für Entwickler

### Build

    $ mvn clean compile
    
### Anwendung ausführen

    $ mvn clean compile exec:java

### Bundle

    $ mvn clean package

## Release
Um eine verteilbare Version des Downloadclienten zu erzeugen, führen Sie
`build.sh` aus dem `misc`-Verzeichnis aus.
Dies erzeugt eine Zip-Datei. In der Zip-Datei sind die Binärdateien
für ogr2ogr auf Windows inkludiert.
Um `build.sh` auszuführen benötigen Sie `xmlstartlet`, `wget` und `bash`.

## Lizenz

Der Download-Client ist als Freie Software unter der Apache License 2.0 veröffentlicht (Details s. [LICENSE](LICENSE)-Datei).
