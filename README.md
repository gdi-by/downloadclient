# GDI-BY DownloadClient 
[![Build Status](https://travis-ci.org/Intevation/downloadclient.svg?branch=master)][travis]
[![Build status](https://ci.appveyor.com/api/projects/status/enidrbgie64k7ypg?svg=true)][appveyor]
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)][license]
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=de.bayern.gdi%3Adownloadclient)][sonarcube]
[![Quality Gate](https://sonarcloud.io/api/badges/measure?key=de.bayern.gdi%3Adownloadclient&metric=lines)][sonarcube]
[![Quality Gate](https://sonarcloud.io/api/badges/measure?key=de.bayern.gdi%3Adownloadclient&metric=bugs)][sonarcube]
[![Quality Gate](https://sonarcloud.io/api/badges/measure?key=de.bayern.gdi%3Adownloadclient&metric=vulnerabilities)][sonarcube]

[travis]:   https://travis-ci.org/Intevation/downloadclient
[appveyor]: https://ci.appveyor.com/project/intevation/downloadclient
[license]:  https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)
[sonarcube]: https://sonarcloud.io/dashboard?id=de.bayern.gdi%3Adownloadclient


Der Download-Client ist eine Desktop-Anwendung zum einfachen Herunterladen von Geodaten, die über Downloaddienste verfügbar sind. Für die heruntergeladenen Geodaten können optional Weiterverarbeitungsschritte (z. B. Formatkonvertierung) definiert und ausgeführt werden. Die Konfiguration der Download- und Weiterverarbeitungsschritte kann darüber hinaus abgespeichert und über ein Konsolenprogramm erneut ausgeführt werden.

## Systemvoraussetzungen

Für die Ausführung des Download-Clients wird mindestens Java 1.8.0.40 und JavaFX benötigt.

Aktuelle Java-Versionen können hier heruntergeladen werden: http://www.oracle.com/technetwork/java/javase/downloads/index.html


## Dokumentation

Eine Anwender-Dokumentation steht hier bereit: http://downloadclient-gdi-by.readthedocs.io/de/docs/index.html


## Hinweise für Entwickler

### Anwendung bauen 

    $ mvn clean compile

### Anwendung ausführen

    $ mvn clean compile exec:java

### Anwendung explizit testen

    $ mvn test

### Bundle erstellen

    $ mvn clean package

## Release erstellen
Um eine verteilbare Version des Downloadclienten zu erzeugen, führen Sie
`build.sh` aus dem `misc`-Verzeichnis aus.
Dies erzeugt eine Zip-Datei. In der Zip-Datei sind die Binärdateien
für ogr2ogr auf Windows inkludiert.
Um `build.sh` auszuführen benötigen Sie `xmlstartlet`, `wget` und `bash`.

## DEBUG BUILDS USING TRAVIS DOCKER CONTAINERS

Mehr Hinweise unter [Troubleshooting Locally in a Docker Image](https://docs.travis-ci.com/user/common-build-problems/#Troubleshooting-Locally-in-a-Docker-Image)

```
$ docker run -it travisci/ci-garnet:packer-1503972846 /bin/bash
$ su - travis
$ git clone https://github.com/gdi-by/downloadclient
$ mvn clean compile
```

## Lizenz

Der Download-Client ist als Freie Software unter der Apache License 2.0 veröffentlicht (Details s. [LICENSE](LICENSE)-Datei).
