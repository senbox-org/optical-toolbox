package eu.esa.opt.slstr.pdu.stitching.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Tonio Fincke
 */
class ManifestTestUtils {

    static Node createNode(String input) throws IOException, ParserConfigurationException, SAXException {
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))));
    }

    static Document createDocument() throws ParserConfigurationException {
        final DocumentBuilder documentBuilder;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.newDocument();
    }

}
