package eu.esa.opt.dataio.flex.header;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlexHeaderParser {

    private static final String NS_GML = "http://www.opengis.net/gml/3.2";
    private static final String NS_OM = "http://www.opengis.net/om/2.0";
    private static final String NS_EOP = "http://www.opengis.net/eop/2.1";
    private static final String NS_OWS = "http://www.opengis.net/ows/2.0";
    private static final String NS_XLINK = "http://www.w3.org/1999/xlink";

    public FlexProductHeader parse(Path xmlFile) throws IOException {
        try (InputStream inputStream = Files.newInputStream(xmlFile)) {
            return parse(inputStream);
        }
    }

    public FlexProductHeader parse(InputStream inputStream) throws IOException {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(inputStream);

            final FlexProductHeader header = new FlexProductHeader();

            parseTimePeriod(doc, header);
            parsePlatformAndInstrument(doc, header);
            parseAcquisitionParameters(doc, header);
            parseDataFileReferences(doc, header);
            parseMetadata(doc, header);

            return header;
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse FLEX header XML", e);
        }
    }

    private void parseTimePeriod(Document doc, FlexProductHeader header) {
        final NodeList timePeriods = doc.getElementsByTagNameNS(NS_GML, "TimePeriod");
        if (timePeriods.getLength() > 0) {
            final Element timePeriod = (Element) timePeriods.item(0);
            header.setStartTime(getElementText(timePeriod, NS_GML, "beginPosition"));
            header.setStopTime(getElementText(timePeriod, NS_GML, "endPosition"));
        }
    }

    private void parsePlatformAndInstrument(Document doc, FlexProductHeader header) {
        final NodeList platforms = doc.getElementsByTagNameNS(NS_EOP, "Platform");
        if (platforms.getLength() > 0) {
            header.setPlatformName(getElementText((Element) platforms.item(0), NS_EOP, "shortName"));
        }

        final NodeList instruments = doc.getElementsByTagNameNS(NS_EOP, "Instrument");
        if (instruments.getLength() > 0) {
            header.setInstrumentName(getElementText((Element) instruments.item(0), NS_EOP, "shortName"));
        }
    }

    private void parseAcquisitionParameters(Document doc, FlexProductHeader header) {
        final NodeList acquisitions = doc.getElementsByTagNameNS(NS_EOP, "Acquisition");
        if (acquisitions.getLength() > 0) {
            final Element acquisition = (Element) acquisitions.item(0);

            final String orbitNumberText = getElementText(acquisition, NS_EOP, "orbitNumber");
            if (!orbitNumberText.isEmpty()) {
                header.setOrbitNumber(Integer.parseInt(orbitNumberText.trim()));
            }

            header.setOrbitDirection(getElementText(acquisition, NS_EOP, "orbitDirection"));
        }
    }

    private void parseDataFileReferences(Document doc, FlexProductHeader header) {
        final List<String> dataFiles = new ArrayList<>();

        final NodeList serviceRefs = doc.getElementsByTagNameNS(NS_OWS, "ServiceReference");
        for (int i = 0; i < serviceRefs.getLength(); i++) {
            final Element ref = (Element) serviceRefs.item(i);
            final String href = ref.getAttributeNS(NS_XLINK, "href");
            if (href != null && !href.isEmpty() && href.endsWith(".nc")) {
                dataFiles.add(href);
            }
        }

        header.setDataFileNames(dataFiles);
    }

    private void parseMetadata(Document doc, FlexProductHeader header) {
        final NodeList metadataElements = doc.getElementsByTagNameNS(NS_EOP, "EarthObservationMetaData");
        if (metadataElements.getLength() > 0) {
            final Element metadata = (Element) metadataElements.item(0);

            header.setProductName(getElementText(metadata, NS_EOP, "identifier"));
            header.setProductType(getElementText(metadata, NS_EOP, "productType"));

            parseProcessingInfo(metadata, header);
            parseVendorSpecific(metadata, header);
        }
    }

    private void parseProcessingInfo(Element metadata, FlexProductHeader header) {
        final NodeList processingInfos = metadata.getElementsByTagNameNS(NS_EOP, "ProcessingInformation");
        if (processingInfos.getLength() > 0) {
            final Element processingInfo = (Element) processingInfos.item(0);
            header.setProcessorName(getElementText(processingInfo, NS_EOP, "processorName"));
            header.setProcessorVersion(getElementText(processingInfo, NS_EOP, "processorVersion"));
        }
    }

    private void parseVendorSpecific(Element metadata, FlexProductHeader header) {
        final Map<String, String> vendorMap = new LinkedHashMap<>();

        final NodeList specificInfos = metadata.getElementsByTagNameNS(NS_EOP, "SpecificInformation");
        for (int i = 0; i < specificInfos.getLength(); i++) {
            final Element info = (Element) specificInfos.item(i);
            final String attribute = getElementText(info, NS_EOP, "localAttribute");
            final String value = getElementText(info, NS_EOP, "localValue");
            if (!attribute.isEmpty()) {
                vendorMap.put(attribute, value);
            }
        }

        header.setVendorSpecific(vendorMap);
    }

    private String getElementText(Element parent, String namespaceURI, String localName) {
        final NodeList elements = parent.getElementsByTagNameNS(namespaceURI, localName);
        if (elements.getLength() > 0) {
            final String text = elements.item(0).getTextContent();
            return text != null ? text.trim() : "";
        }
        return "";
    }
}
