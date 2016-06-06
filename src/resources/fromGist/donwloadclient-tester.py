#!/usr/bin/env python
# -*- coding: utf-8 -*-

__author__ = 'weich_ju'
import requests
from requests.auth import HTTPBasicAuth
from lxml import etree


########################################################################################################################

def printxpath(xpath, xml):
    namespaces={ #WFS, ATOM
                'wfs': 'http://www.opengis.net/wfs/2.0',
                'fes': 'http://www.opengis.net/fes/2.0',
                'ows': 'http://www.opengis.net/ows/1.1',
                'gml': 'http://www.opengis.net/gml/3.2',

                'gmd': 'http://www.isotc211.org/2005/gmd',
                'gco': 'http://www.isotc211.org/2005/gco',
                'srv': 'http://www.isotc211.org/2005/srv',
                'csw': 'http://www.opengis.net/cat/csw/2.0.2',

                'atom': 'http://www.w3.org/2005/Atom',
                'inspire_dls': 'http://inspire.ec.europa.eu/schemas/inspire_dls/1.0',

                'xlink': 'http://www.w3.org/1999/xlink',
                }

    print(xpath, xml.xpath(xpath, namespaces=namespaces))


# Holt etree XML über HTTP-GET-Request
def do_get(url, username='', password=''):
    print('--> DO GET', url)
    if username and password:
        response = requests.get(url, auth=HTTPBasicAuth(username, password))
    else:
        response = requests.get(url)
    xmlstring = response.text.replace('<?xml version="1.0" encoding="UTF-8"?>', '')
    return etree.fromstring(xmlstring)

# Holt etree XML über HTTP-POST-Request
def do_post(url, post_data_url):
    print('--> DO POST', url)
    data = requests.get(post_data_url).text
    headers = {'content-type': 'application/xml'}
    response = requests.post(url, data=data, headers=headers)
    xmlstring = response.text.replace('<?xml version="1.0" encoding="UTF-8"?>', '')
    return etree.fromstring(xmlstring)



########################################################################################################################


if  __name__ =='__main__':

    # ServiceMetadata

    print('\nServiceMetadata aus CSW-Response')
    xml = do_post("http://geoportal.bayern.de/csw/gdi", "https://gist.githubusercontent.com/gdi-by/a458fe1420ac17fb719c/raw/fe22c1ebbfd068997f1de961556e57485380df73/csw-post-request.xml")
    printxpath("//csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString/text()", xml)
    printxpath("//csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/gmd:abstract/gco:CharacterString/text()", xml)
    printxpath("//csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:containsOperations/srv:SV_OperationMetadata/srv:connectPoint/gmd:CI_OnlineResource/gmd:linkage/gmd:URL/text()", xml)
    printxpath("//csw:GetRecordsResponse/csw:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:serviceTypeVersion/gco:CharacterString/text()", xml)


    print('\nServiceMetadata aus GetCapabilities-Response')
    xml = do_get('http://geoserv.weichand.de:8080/geoserver/wfs?service=WFS&acceptversions=2.0.0&request=GetCapabilities')
    printxpath("//wfs:WFS_Capabilities/ows:ServiceIdentification/ows:Title/text()", xml)
    printxpath("//wfs:WFS_Capabilities/ows:ServiceIdentification/ows:Abstract/text()", xml)
    printxpath("//wfs:WFS_Capabilities/ows:OperationsMetadata/ows:Operation/ows:DCP/ows:HTTP/ows:Get/@xlink:href", xml)


    # DatasetQuery

    print('\nDatasetQuery aus Simple-WFS')
    xml = do_get('http://geoserv.weichand.de:8080/geoserver/wfs?service=WFS&version=2.0.0&request=DescribeStoredQueries')
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/@id", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Title/text()", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Abstract/text()", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Parameter/@name", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Parameter/@type", xml)


    print('\nDatasetQuery aus Basic-WFS')
    xml = do_get('http://geoserv.weichand.de:8080/geoserver/wfs?service=WFS&acceptversions=2.0.0&request=GetCapabilities')
    printxpath("//wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Name/text()", xml)
    printxpath("//wfs:WFS_Capabilities/wfs:FeatureTypeList/wfs:FeatureType/wfs:Title/text()", xml)


    # DatasetQuery - kennwortgeschützter Dienst

    print('\nDatasetQuery aus Simple-WFS')
    xml = do_get('https://www.geodaten.bayern.de/wfs/ogc_hauskoordinaten.cgi?service=WFS&version=2.0.0&request=DescribeStoredQueries', username='{USERNAME}', password='{PASSWORD}')
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/@id", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Title/text()", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Abstract/text()", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Parameter/@name", xml)
    printxpath("//wfs:DescribeStoredQueriesResponse/wfs:StoredQueryDescription/wfs:Parameter/@type", xml)


    # AtomFeed
    print('\nServiceFeedQuery aus Atom')
    xml = do_get('https://geoportal.bayern.de/gdiadmin/ausgabe/ATOM_SERVICE/d408a036-9c71-4682-8391-67ff729d890d?service=WFS&acceptversions=2.0.0&request=GetCapabilities')
    printxpath("//atom:feed/atom:entry/atom:title/text()", xml)
    printxpath("//atom:feed/atom:entry/atom:summary/text()", xml)
    printxpath("//atom:feed/atom:entry/inspire_dls:spatial_dataset_identifier_code/text()", xml)


    print('\nDatasetFeedQuery aus Atom')
    xml = do_get('https://geoportal.bayern.de/gdiadmin/ausgabe/ATOM_DATASET/d408a036-9c71-4682-8391-67ff729d890d?service=WFS&acceptversions=2.0.0&request=GetCapabilities')
    printxpath("//atom:feed/atom:entry/atom:title/text()", xml)
    printxpath("//atom:feed/atom:entry/atom:link/@type", xml)
    printxpath("//atom:feed/atom:entry/atom:category/@term", xml)
    printxpath("//atom:feed/atom:entry/atom:link/@href", xml)
