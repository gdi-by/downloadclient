=======================================================
GDI-BY Download-Client Dokumentation
=======================================================

:Autor: Geschäftsstelle Geodateninfrastruktur Bayern (GDI-BY)
:Kontakt: gdi-by@ldbv.bayern.de


Einleitung
============

Zur einfacheren Nutzung der im Rahmen der Geodateninfrastruktur Bayern (GDI-BY) bereitgestellten Downloaddienste steht der Download-Client als zentrale Komponente zur Verfügung. Der Download-Client ist eine plattformunabhängige OpenSource-Desktopanwendung. 

Nach dem reinen Download der Daten können Downloadschritte auch konfiguriert und abgespeichert werden. Die gespeicherten Downloadschritte können anschließend über ein Konsolenprogramm angestoßen und automatisiert ausgeführt werden. 


Installation
============

Voraussetzungen - Softwareumgebung
------------------------------------

**Für die Installation des Download-Clients wird mindestens Java 1.8.0.40 benötigt.**

Windows
---------

Die Software benötigt Java 8 mit JavaFX. Das aktuelle JRE kann über http://www.oracle.com/technetwork/java/javase/downloads/index.html heruntergeladen werden. 


Werden nach dem Download die Daten weiterverarbeitet, so werden weitere Tools wie ogr2ogr benötigt. Dazu kann z.B. die gdal-Bibliothek installiert werden.  


Linux
-----
Der Download-Client wurde mit OpenJDK auf Debian und Arch Linux entwickelt. Wird eine aktuelle OpenJDK-Version verwendet sind keine weiteren Voraussetzungen nötig. 

Werden nach dem Download die Daten weiterverarbeitet, so werden weitere Tools wie ogr2ogr benötigt. Diese sollten über den Package Manager installiert werden. 

Befehle 
--------

Im XML *download-steps.xml* können ein oder mehrere Download-Schritt-Dateien enthalten sein. Nach der Erstellung des jar-files soll folgender Befehl ausgeführt werden: 

     $ java -jar target/downloadclient-1.0-SNAPSHOT.jar

oder im *headless mode*:

     $ java -jar target/downloadclient-1.0-SNAPSHOT.jar --headless [download-steps.xml]

Um passwortgeschützte Dienste zugänglich zu machen, können die Benutzerdaten über  --user=<user> and --password=<password> dem Download-Client übergeben werden.

     `--config=<directory>`

Hier kann ein Ordner spezifiziert werden mit Konfigurationsdateien, um die default-Einstellungen zu überschreiben (wichtig für Proxy-Einstellungen!):  

     `proxy.xml` 

Um alternative HTTP(S) Proxy-Einstellungen zu konfigurieren.

<!-- HTTP settings: --> HOST PORT USER PASSWORD HOST1|HOST2|... <!-- HTTPS settings: --> HOST PORT USER PASSWORD HOST1|HOST2|...

All fields are optional. To avoid application of the settings set overrideSystemSetting="false". enableSNIExtension enables/disables Server Name Indication. This might be needed in case of some problematic SSL-Hosts.

     `serviceSetting.xml`
Ein vorselektiertes Dienste-Set. 

     `verarbeitungsschritte.xml`
Eine Liste von Verarbeitungsschritten nach einem erfolgreichen Download.


Funktionalität
==============

Unterstützte Implementierungsvarianten
---------------------------------------

Aktuell werden folgende Downloaddienste vom Download-Client der GDI-BY unterstützt:

+---------------+---------------------+----------------------------+
| Modul         | Downloaddienst      | Konformitätsklasse         |
+===============+=====================+============================+
| 1             | WFS 2.0             |  simple WFS                |
+---------------+---------------------+----------------------------+
| 2             | WFS 2.0             |  basic WFS                 |
+---------------+---------------------+----------------------------+
| 3             | predefined ATOM     |                            |
+---------------+---------------------+----------------------------+


Auswahl von Downloaddiensten
------------------------------
Downloaddienste können über zwei Varianten eingebunden werden: 

- Eingabe der URL eines Downloaddienstes (vollständige GetCapabilities-URL inkl. Paramater bei WFS oder URL des predefined ATOM Downloaddienstes) - weitere Einschränkungen siehe "unterstützte Implementierungsvarianten" 

- Suche nach Downloaddiensten über das Suchfeld. Hier wird im Hintergrund ein CSW GetRecordbyID-Aufruf auf den CSW http://geoportal.bayern.de/csw/gdi? mit einem Filter *ServiceTypeVersion = OGC:WFS:2.0* oder *ATOM* durchgeführt.


Beispiel-URLs sind:

- http://geoserv.weichand.de:8080/geoserver/wfs?service=WFS&acceptversions=2.0.0&request=getCapabilities (WFS 2.0.0)
- https://geoportal.bayern.de/gdiadmin/ausgabe/ATOM_SERVICE/4331d3ef-a12d-48be-a9b9-9597c2591448 (predefined Atom)
- http://www.geodaten.bayern.de/inspire/dls/dop200.xml (predefined Atom)

Über den Button *Dienst wählen* kann ein Downloaddienst eingebunden werden. Bei passwortgeschützten Diensten müssen die Login-Daten entsprechend in den Feldern *Kennung* und *Passwort* eingetragen werden. 

Ist nicht bekannt, ob ein Dienst passwortgeschützt ist oder nicht, so kann einfach die URL in das entsprechende Feld eingetragen werden. Nach einer Überprüfung wird vom Client gegebenenfalls die Meldung *"Service ist zugangsbeschränkt. Geben Sie Nutzername und Passwort an."* angezeigt.

Im Hintergrund wird dabei bei WFS-Downloaddiensten ein HTTP-GET Aufruf gestartet. 
Bei predefined ATOM-Downloaddiensten wird eine HTTP-GET Capabilities Aufruf auf die entsprechende online Ressource sowie ein HTTP-HEAD Aufruf auf die dataset feed Einträge (Pfad: //feed/entry[1]/link[1]@href) durchgeführt.

Die grafische Benutzeroberfläche passt sich je nach Art des gewählten Downloaddienstes und Konformitätsklasse entsprechend an: 

Download von Datensätzen eines WFS 2.0 
---------------------------------------

Beim Download von Datensätzen eines WFS 2.0 werden in der Dropdown-Liste sowohl alle FeatureTypes des WFS als auch alle vordefinierten Abfragen - stored queries (wenn vorhanden) zum Download angeboten. 
Standardmäßig ist der erste Eintrag der Dropdown-Liste ausgewählt.
 
*********************
Vordefinierte Abfrage
*********************

Handelt es sich beim Eintrag um eine vordefinierte Abfrage, passt sich die Oberfläche dahingehend an, dass als Eingabefelder die Abfrageparameter erscheinen. Zusätzlich kann ein Ausgabedatenformat gewählt werden.

**Beispiel:**

.. image:: img/DLC_storedquery_WFS.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Gemeinde"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Verwaltungsgrenzen - WFS 2.0 DemoServer"* verwendet. Die vordefinierte Abfrage lautet *"Abfrage einer Gemeinde über den Gemeindeschlüssel"*. 
Dabei wird die Grenze der Stadt München mit dem Schlüssel *09162000* im Format *KML* abgefragt.

************
FeatureTypes
************

Handelt es sich um ein FeatureType, so kann der Nutzer über die Kartenkomponente eine BoundingBox aufziehen und so den Bereich wählen, für welchen er Daten beziehen möchte. 
Zusätzlich kann noch ein Ausgabedatenformat und ein Koordinatenreferenzsystem gewählt werden, welche vom WFS nativ unterstützt werden. 

**Beispiel:**

.. image:: https://github.com/gdi-by/downloadclient/blob/docs/docs/img/DLC_featuretype_WFS.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Gemeinde"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Verwaltungsgrenzen - WFS 2.0 DemoServer"* verwendet. Anschließend wird der FeatureType *"GemeindenBayern"* ausgewählt und auf der Karte ein Rechteck aufgezogen (=Begrenzungsfläche definiert). Somit können sämtliche Gemeindegrenzen heruntergeladen werden, welche sich mit dem Begrenzungsrechteck berühren. Als Ausgabedatenformat wird *KML* gewählt, das Koordinatenreferenzsystem soll *WGS84* sein.

Download von Datensätzen eines predefined ATOM Downloaddienstes
------------------------------------------------------------------

Beim Download von Datensätzen eines predefined ATOM Downloaddienstes werden in der Dropdown-Liste alle verfügbaren ServiceFeed-Einträge (=Datensätze) zum Download angeboten. Standardmäßig ist der erste Eintrag der Drowpdown-Liste ausgewählt. 

Der Nutzer hat die Möglichkeit, die Auswahl durch Wahl eines anderen Eintrags der Liste oder durch Wahl eines Bereiches in der Kartenkomponente zu ändern. 

Einschränkung: Die Auswahl eines Datensatzes über die Kartenkomponente ist nur dann möglich, wenn die geographische Begrenzung der einzelnen Datensätze sich nicht überlagern. 

**Beispiel Variante a):**

.. image:: https://github.com/gdi-by/downloadclient/blob/docs/docs/img/DLC_Kartenauswahl_Atom.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"digitales Orthophoto"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Digitales Orthophoto 2 m Bodenauflösung - ATOM-Feed"* verwendet.
Der Dienst stellt Datensätze mit unterschiedlichen geographischen Begrenzungen zum Download zur Auswahl. Somit ist eine Auswahl über die Kartenkomponente möglich. Es wird der Datensatz *"Digitales Orthophoto 112013-0"* in der Variante *"Gauß-Krueger Zone 4"* gewählt. 



**Beispiel Variante b):**

.. image:: https://github.com/gdi-by/downloadclient/blob/docs/docs/img/DLC_Listenauswahl_Atom.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Naturschutz"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Schutzgebiete des Naturschutzes - Downloaddienst"* verwendet.
Der Dienst bietet die Datensätze Naturparke, Nationalparke, Naturschutzgebiete, Biosphärenreservate und Landschaftsschutzgebiete zum Download zur Auswahl. 
Da die Datensätze jeweils eine bayernweite Ausdehnung haben, ist nur eine Auswahl über die Dropdown-Liste möglich.
Es wird der Datensatz *"Nationalparke"* in der Variante *"Gauß-Krueger Zone 4"* gewählt. 


Verarbeitungskette
-------------------

Die heruntergeladenen Datensätze  können mit Hilfe des Clients zu einem individuellen Endergebnis weiterverarbeitet werden (=Verarbeitungskette). 

Die zur Verfügung stehenden Verarbeitungsschritte können durch Anpassung der Verarbeitungskonfigurations-Datei (siehe xxxx)  bei Bedarf durch den Anwender beliebig ergänzt und konfiguriert werden.

Folgende Verarbeitungsschritte werden aktuell von der Geschäftsstelle GDI-BY zur Verfügung gestellt: 

- Konvertierung eines Vektordatenformates nach ESRI-Shape nach Eingabe folgendes Parameters: 
   - Koordinatenreferenzsystem 

- Konvertierung eines Rasterdatenformates nach GeoTIFF nach Eingabe folgendes Parameters:
   - Koordinatenreferenzsystem

- ??? Konvertierung eines Vektor- oder Rasterdatenformates nach GeoPackage nach Eingabe folgender Parameter: 

Es ist möglich, mehrere Verarbeitungsschritte nacheinander durchzuführen.


Ausführungswiederholung
---------------------------

Eine Download-Konfiguration kann über den entsprechenden Button gespeichert werden und ist automatisiert über ein Konsolenprogramm erneut ausführbar. 
 
!!!!!!!! Beispiele für Konfigurations-Dateien der Download-Schritte stehen unter folgenden Links zur Verfügung: 

- https://gist.github.com/gdi-by/b5ade5062477eae11391 (Atom)

- https://gist.github.com/gdi-by/ebfa67fbda614fa30e59 (WFS2 Simple - Beispiel mit Weiterverarbeitung)

- https://gist.github.com/gdi-by/d02e71e0bb1c1ac21cd7 (WFS2 Basic)

- Das entsprechende Schema befindet sich unter https://gist.github.com/gdi-by/20b132cfd5d34abb147a


Lizenz
======

Der Download-Client ist eine OpenSource-Software und steht unter der Lizenz "Apache License 2.0".
Nähere Details befinden sich unter *LICENSE*.




Entwicklerhinweise
==================

Der GDI-BY Download-Client kann mit Maven kompiliert werden.


Build 

      $ mvn clean compile 

Bundle 

      $ mvn clean package


Ausführen mit Benutzeroberfläche

     '$ mvn exec:java'

Ausführen im *headless mode*:

     $ mvn exec:java -Dexec.args=-headless [download-steps.xml ...]





