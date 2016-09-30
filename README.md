# GDI-BY DownloadClient 
[![Build Status](https://travis-ci.org/gdi-by/downloadclient.svg?branch=master)](https://travis-ci.org/gdi-by/downloadclient)

Der Download-Client ist eine Desktop-Anwendung zum einfachen Herunterladen von Geodaten, die über Downloaddienste verfügbar sind. Für die heruntergeladenen Geodaten können optional Weiterverarbeitungsschritte (z. B. Formatkonvertierung) definiert und ausgeführt werden. Die Konfiguration der Download- und Weiterverarbeitungsschritte kann darüber hinaus abgespeichert und über ein Konsolenprogramm erneut ausgeführt werden.

## Systemvoraussetzungen

Für die Ausführung des Download-Clients wird mindestens Java 1.8.0.40 benötigt.

Aktuelle Java-Versionen können hier heruntergeladen werden: http://www.oracle.com/technetwork/java/javase/downloads/index.html


## Dokumentation

Eine Anwender-Dokumentation steht hier bereit: http://downloadclient-gdi-by.readthedocs.io/de/docs/index.html


## Hinweise für Entwickler

### Build

    $ mvn clean compile

### Bundle

    $ mvn clean package


## Lizenz

Der Download-Client ist als Freie Software unter der Apache License 2.0 veröffentlicht (Details s. LICENSE-Datei).
