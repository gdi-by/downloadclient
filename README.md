# GDI-BY DownloadClient
[![Build Status](https://travis-ci.org/gdi-by/downloadclient.svg?branch=master)](https://travis-ci.org/gdi-by/downloadclient)


Der Download-Client ist eine Desktop-Anwendung zum einfachen Herunterladen von Geodaten, die über Downloaddienste verfügbar sind. Für die heruntergeladenen Geodaten können optional Weiterverarbeitungsschritte (z. B. Formatkonvertierung) definiert und ausgeführt werden. Die Konfiguration der Download- und Weiterverarbeitungsschritte kann darüber hinaus abgespeichert und über ein Konsolenprogramm erneut ausgeführt werden.

## Systemvoraussetzungen

Für die Ausführung des Download-Clients wird Java 1.8.0 oder Java 11.0 mit JavaFX benötigt.

Aktuelle Java-Versionen können hier heruntergeladen werden: http://www.oracle.com/technetwork/java/javase/downloads/index.html


## Dokumentation

Eine Anwender-Dokumentation steht hier bereit: http://downloadclient-gdi-by.readthedocs.io/de/docs/index.html


## Hinweise für Entwickler

### Anwendung bauen

    $ mvn clean compile

### Anwendung ausführen

    $ mvn clean compile exec:java

### Anwendung testen

    $ mvn test

#### Integrationstest ausführen 

    $ mvn verify

### Bundle erstellen

    $ mvn clean package

### Quelltext Analyse mittels `SonarQube Scanner for Maven` erstellen

```
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.organization=<<organization>>\
    -Dsonar.login=<<TOKEN>>
```

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
$ cd downloadclient
$ mvn clean compile
```

## Lizenz

Der Download-Client ist als Freie Software unter der Apache License 2.0 veröffentlicht (Details s. [LICENSE](LICENSE)-Datei).
