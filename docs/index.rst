=======================================================
GDI-BY Download-Client Dokumentation
=======================================================

:Autor: Geschäftsstelle Geodateninfrastruktur Bayern (GDI-BY)
:Kontakt: gdi-by@ldbv.bayern.de


Einleitung
============

Der Download-Client ist eine Desktop-Anwendung zum einfachen Herunterladen von Geodaten, die über Downloaddienste verfügbar sind. Für die heruntergeladenen Geodaten können optional Weiterverarbeitungsschritte (z. B. Formatkonvertierung) definiert und ausgeführt werden. Die Konfiguration der Download- und Weiterverarbeitungsschritte kann darüber hinaus abgespeichert und über ein Konsolenprogramm erneut ausgeführt werden.

Der Download-Client wird von der Geodateninfrastruktur Bayern (GDI-BY) als Open-Source-Software unter der Apache License 2.0 zur Verfügung gestellt.



Installation
============

Installationspakete für Windows und Linux stehen vrstl. ab Oktober 2016 hier bereit: http://gdi.bayern/downloadclient

Voraussetzungen - Softwareumgebung
------------------------------------

**Für die Installation des Download-Clients wird mindestens Java 1.8.0.40 benötigt.**

Aktuelle Java-Versionen können hier heruntergeladen werden: http://www.oracle.com/technetwork/java/javase/downloads/index.html


Funktionalität
==============

Unterstützte Downloaddienstvarianten
---------------------------------------

Aktuell werden folgende INSPIRE-Downloaddienstvarianten* vom Download-Client unterstützt:

+-------------------------------------+--------------------------------+----------------------------+
| Variante                            | Standard                       | Konformitätsklasse         |
+=====================================+================================+============================+
| Pre-defined Dataset Downlaod        | Web Featrue Service (WFS)  2.0 |  Simple WFS                |
+-------------------------------------+--------------------------------+----------------------------+
| Direct Access Download              | WFS 2.0                        |  Basic WFS                 |
+-------------------------------------+--------------------------------+----------------------------+
| Pre-defined Dataset Download        | predefined ATOM                |                            |
+-------------------------------------+--------------------------------+----------------------------+

*gemäß der Technical Guidance for the Implementation of INSPIRE Download Services, Version 3.1, s. http://inspire.jrc.ec.europa.eu/documents/Network_Services/Technical_Guidance_Download_Services_v3.1.pdf 

Benutzeroberfläche 
-------------------

.. image:: img/DLC_GUI.png



Auswahl von Downloaddiensten
------------------------------
Downloaddienste können über verschiedene Wege eingebunden werden: 

- Eingabe der URL eines Downloaddienstes (vollständige GetCapabilities-URL inkl. Paramater bei WFS oder URL des ATOM Downloaddienstes) 

- Suche nach Downloaddiensten durch Eingabe eines Suchbegriffes in das Suchfeld. Hier wird im Hintergrund ein GetRecord-Aufruf an einen Metadatenkatalogdienst (CSW) mit einem Filter *ServiceTypeVersion = OGC:WFS:2.0* oder *ATOM* durchgeführt. Standardmäßig ist hier der Metadatenkatalog der GDI-BY (http://geoportal.bayern.de/csw/gdi?) eingebunden. Das Einbinden anderer Kataloge ist möglich (s. Abschnitt „Benutzerdefinierte Erweiterungsmöglichkeiten“)


Beispiel-URLs sind:

- http://geoserv.weichand.de:8080/geoserver/wfs?service=WFS&acceptversions=2.0.0&request=getCapabilities (WFS 2.0.0)
- https://geoportal.bayern.de/gdiadmin/ausgabe/ATOM_SERVICE/4331d3ef-a12d-48be-a9b9-9597c2591448 (Atom)
- http://www.geodaten.bayern.de/inspire/dls/dop200.xml (Atom)

Über den Button *Dienst wählen* kann ein Downloaddienst eingebunden werden. Bei zugriffsgeschützten Diensten müssen die Zugangsdaten entsprechend in den Feldern *Kennung* und *Passwort* eingetragen werden. 

Ist nicht bekannt, ob ein Dienst passwortgeschützt ist oder nicht, so kann einfach die URL in das entsprechende Feld eingetragen werden. Nach einer Überprüfung wird vom Client gegebenenfalls die Meldung *"Service ist zugangsbeschränkt. Geben Sie Nutzername und Passwort an."* angezeigt.

Die grafische Benutzeroberfläche passt sich je nach der gewählten Downloaddienstvariante automatisch an: 

Download von Datensätzen eines WFS 2.0 
---------------------------------------

Beim Download von Datensätzen eines WFS 2.0 werden in der Datensatz-Auswahlliste sowohl alle FeatureTypes des WFS als auch alle vordefinierten Abfragen ("Stored Queries" - wenn vorhanden) zum Download angeboten. 
Standardmäßig ist der erste Eintrag der Liste ausgewählt.
 
*********************
Vordefinierte Abfrage
*********************

Bei Auswahl einer vordefinierten Abfrage passt sich der Datensatzvarianten-Auswahlbereich dahingehend an, dass die Abfrageparameter als Eingabefelder sowie (falls vorhanden) eine Beschreibung der vordefinierten Abfrage erscheinen. Zusätzlich kann eines der vom Dienst nativ angebotenen Ausgabedatenformate gewählt werden.

**Beispiel:**

.. image:: img/DLC_storedquery_WFS.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Gemeinde"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Verwaltungsgrenzen - WFS 2.0 DemoServer"* verwendet. Die vordefinierte Abfrage lautet *"Abfrage einer Gemeinde über den Gemeindeschlüssel"*. 
Dabei wird die Grenze der Stadt München mit dem Schlüssel *09162000* im Format *KML* abgefragt. Mit Klick auf den Button „Download start...“ unter Angabe eines Zielordners wird der Download angestoßen.

************
FeatureTypes
************

Handelt es sich um ein FeatureType, so kann der Nutzer über die Kartenkomponente ein Begrenzungsrechteck aufziehen und so den Bereich wählen, für welchen er Daten beziehen möchte. 
Zusätzlich kann noch ein Ausgabedatenformat und ein Koordinatenreferenzsystem gewählt werden, welche vom WFS nativ unterstützt werden. 

**Beispiel:**

.. image:: img/DLC_featuretype_WFS.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Gemeinde"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Verwaltungsgrenzen - WFS 2.0 DemoServer"* verwendet. Anschließend wird der FeatureType *"GemeindenBayern"* ausgewählt und auf der Karte ein Rechteck aufgezogen. Somit können sämtliche Gemeindegrenzen heruntergeladen werden, welche sich mit dem Begrenzungsrechteck berühren. Als Ausgabedatenformat wird *KML* gewählt, das Koordinatenreferenzsystem soll *WGS84* sein.

Download von Datensätzen eines predefined ATOM Downloaddienstes
------------------------------------------------------------------

Beim Download von Datensätzen eines predefined ATOM Downloaddienstes werden in der Datensatz-Auswahlliste alle verfügbaren ServiceFeed-Einträge (=Datensätze) zum Download angeboten. Standardmäßig ist der erste Eintrag der Liste ausgewählt. 

Der Nutzer hat die Möglichkeit, die Auswahl durch Wahl eines anderen Eintrags der Liste oder durch Wahl eines Bereiches in der Kartenkomponente zu ändern. 

Einschränkung: Die Auswahl eines Datensatzes über die Kartenkomponente ist nur dann möglich, wenn die geographischen Begrenzungspolygone der einzelnen Datensätze sich nicht überlagern. 

**Beispiel Variante a):**

.. image:: img/DLC_Kartenauswahl_Atom.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"digitales Orthophoto"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Digitales Orthophoto 2 m Bodenauflösung - ATOM-Feed"* verwendet.
Der Dienst stellt Datensätze mit unterschiedlichen geographischen Begrenzungspolygonen zum Download zur Auswahl. Somit ist eine Auswahl über die Kartenkomponente möglich. Es wird der Datensatz *"Digitales Orthophoto 112013-0"* in der Variante *"Gauß-Krueger Zone 4"* (EPSG:31468) gewählt. 



**Beispiel Variante b):**

.. image:: img/DLC_Listenauswahl_Atom.PNG


Im oben dargestellten Beispiel wird als Suchbegriff *"Naturschutz"* im entsprechenden Suchfenster eingegeben und der Downloaddienst *"Schutzgebiete des Naturschutzes - Downloaddienst"* verwendet.
Der Dienst bietet die Datensätze Naturparke, Nationalparke, Naturschutzgebiete, Biosphärenreservate und Landschaftsschutzgebiete zum Download zur Auswahl. 
Da die Datensätze jeweils eine bayernweite Ausdehnung haben, ist nur eine Auswahl über die Dropdown-Liste möglich.
Es wird der Datensatz *"Nationalparke"* in der Variante *"Gauß-Krueger Zone 4"* gewählt. 


Weiterverarbeitung der heruntergeladenen Datensätze
-------------------

Die heruntergeladenen Datensätze  können mit Hilfe des Download-Clients zu einem individuellen Endergebnis weiterverarbeitet werden (=Verarbeitungskette). 

Nach Anhaken von "Weiterverarbeiten" können über den Button "Hinzufügen" ein oder mehrere Verarbeitungsschritte hinzugefügt werden.

Folgende Verarbeitungsschritte stehen bereits vorkonfiguriert zur Verfügung: 

- Konvertierung eines Vektordatenformates nach ESRI-Shape nach Eingabe des folgenden Parameters: 
   - Koordinatenreferenzsystem 

- Konvertierung eines Rasterdatenformates nach GeoTIFF nach Eingabe des folgenden Parameters:
   - Koordinatenreferenzsystem

Die zur Verfügung stehenden Verarbeitungsschritte können durch Anpassung der Verarbeitungskonfigurations-Datei (s.u. „Benutzerdefinierte Erweiterungsmöglichkeiten) bei Bedarf durch den Anwender beliebig ergänzt und konfiguriert werden.


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





