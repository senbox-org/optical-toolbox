package eu.esa.opt.dataio.flex.header;

import com.bc.ceres.annotation.STTM;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


public class FlexHeaderParserTest {


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private FlexHeaderParser parser;

    @Before
    public void setUp() {
        parser = new FlexHeaderParser();
    }


    @Test
    @STTM("SNAP-4126")
    public void testParseL1bHeader() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l1b_header.xml")) {
            header = parser.parse(is);
        }

        assertEquals("FLX_L1B_OBS____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01", header.getProductName());
        assertEquals("L1B_OBS___", header.getProductType());
        assertEquals("2023-07-12T07:59:54.050Z", header.getStartTime());
        assertEquals("2023-07-12T08:02:32.346Z", header.getStopTime());
        assertEquals("FLEX", header.getPlatformName());
        assertEquals("FLORIS", header.getInstrumentName());
        assertEquals(38179, header.getOrbitNumber());
        assertEquals("DESCENDING", header.getOrbitDirection());
        assertEquals("L1PF", header.getProcessorName());
        assertEquals("03.02", header.getProcessorVersion());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL1bHeader_dataFiles() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l1b_header.xml")) {
            header = parser.parse(is);
        }

        final List<String> dataFiles = header.getDataFileNames();
        assertEquals(3, dataFiles.size());
        assertTrue(dataFiles.get(0).endsWith("_hre1.nc"));
        assertTrue(dataFiles.get(1).endsWith("_hre2.nc"));
        assertTrue(dataFiles.get(2).endsWith("_lres.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL1bHeader_vendorSpecific() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l1b_header.xml")) {
            header = parser.parse(is);
        }

        final Map<String, String> vendor = header.getVendorSpecific();
        assertFalse(vendor.isEmpty());
        assertEquals("COMMISSIONING", vendor.get("missionPhase"));
        assertEquals("0158", vendor.get("Duration"));
        assertEquals("0100", vendor.get("Cycle_Number"));
        assertEquals("064", vendor.get("Relative_Orbit_Number"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL1cHeader() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l1c_header.xml")) {
            header = parser.parse(is);
        }

        assertEquals("L1C_FLXSYN", header.getProductType());
        assertEquals("FLEX", header.getPlatformName());
        assertEquals("FLORIS", header.getInstrumentName());
        assertFalse(header.getStartTime().isEmpty());
        assertFalse(header.getStopTime().isEmpty());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL1cHeader_dataFiles() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l1c_header.xml")) {
            header = parser.parse(is);
        }

        final List<String> dataFiles = header.getDataFileNames();
        assertEquals(1, dataFiles.size());
        // note: test products have double .nc.nc extension
        assertTrue(dataFiles.get(0).endsWith(".nc.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL2Header() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l2_header.xml")) {
            header = parser.parse(is);
        }

        assertEquals("L2__FLXSYN", header.getProductType());
        assertEquals("FLEX", header.getPlatformName());
        assertEquals("FLORIS", header.getInstrumentName());
        assertFalse(header.getStartTime().isEmpty());
        assertFalse(header.getStopTime().isEmpty());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL2Header_dataFiles() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l2_header.xml")) {
            header = parser.parse(is);
        }

        final List<String> dataFiles = header.getDataFileNames();
        assertEquals(1, dataFiles.size());
        assertTrue(dataFiles.get(0).endsWith(".nc.nc"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseL2Header_processingInfo() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l2_header.xml")) {
            header = parser.parse(is);
        }

        assertFalse(header.getProcessorName().isEmpty());
        assertFalse(header.getProcessorVersion().isEmpty());
    }


    @Test
    @STTM("SNAP-4126")
    public void testParseCompleteHeader() throws IOException {
        final FlexProductHeader header = parser.parse(stream(completeXml()));

        assertEquals("PRODUCT_NAME", header.getProductName());
        assertEquals("L1B_OBS___", header.getProductType());
        assertEquals("2023-07-12T07:59:54.050Z", header.getStartTime());
        assertEquals("2023-07-12T08:02:32.346Z", header.getStopTime());
        assertEquals("FLEX", header.getPlatformName());
        assertEquals("FLORIS", header.getInstrumentName());
        assertEquals(38179, header.getOrbitNumber());
        assertEquals("DESCENDING", header.getOrbitDirection());
        assertEquals("L1PF", header.getProcessorName());
        assertEquals("03.02", header.getProcessorVersion());

        assertEquals(2, header.getDataFileNames().size());
        assertEquals("./data_1.nc", header.getDataFileNames().get(0));
        assertEquals("./data_2.nc.nc", header.getDataFileNames().get(1));

        assertEquals(2, header.getVendorSpecific().size());
        assertEquals("COMMISSIONING", header.getVendorSpecific().get("missionPhase"));
        assertEquals("", header.getVendorSpecific().get("emptyValue"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseFromPath() throws IOException {
        final File file = tempFolder.newFile("header.xml");
        java.nio.file.Files.write(file.toPath(), completeXml().getBytes(StandardCharsets.UTF_8));

        final FlexProductHeader header = parser.parse(file.toPath());

        assertEquals("PRODUCT_NAME", header.getProductName());
        assertEquals("L1B_OBS___", header.getProductType());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseMinimalHeaderUsesDefaults() throws IOException {
        final FlexProductHeader header = parser.parse(stream(
                "<root xmlns:gml=\"http://www.opengis.net/gml/3.2\" " +
                        "xmlns:eop=\"http://www.opengis.net/eop/2.1\" " +
                        "xmlns:ows=\"http://www.opengis.net/ows/2.0\" " +
                        "xmlns:xlink=\"http://www.w3.org/1999/xlink\"/>"
        ));

        assertEquals("", header.getProductName());
        assertEquals("", header.getProductType());
        assertEquals("", header.getStartTime());
        assertEquals("", header.getStopTime());
        assertEquals("", header.getPlatformName());
        assertEquals("", header.getInstrumentName());
        assertEquals(-1, header.getOrbitNumber());
        assertEquals("", header.getOrbitDirection());
        assertEquals("", header.getProcessorName());
        assertEquals("", header.getProcessorVersion());
        assertEquals(Collections.emptyList(), header.getDataFileNames());
        assertEquals(Collections.emptyMap(), header.getVendorSpecific());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseAcquisitionWithoutOrbitNumber() throws IOException {
        final FlexProductHeader header = parser.parse(stream(
                xmlBody(
                        "<eop:Acquisition>" +
                                "<eop:orbitDirection>ASCENDING</eop:orbitDirection>" +
                                "</eop:Acquisition>"
                )
        ));

        assertEquals(-1, header.getOrbitNumber());
        assertEquals("ASCENDING", header.getOrbitDirection());
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseServiceReferencesFiltersOnlyLowercaseNcHref() throws IOException {
        final FlexProductHeader header = parser.parse(stream(
                xmlBody(
                        "<ows:ServiceReference xlink:href=\"valid.nc\"/>" +
                                "<ows:ServiceReference xlink:href=\"valid.nc.nc\"/>" +
                                "<ows:ServiceReference xlink:href=\"\"/>" +
                                "<ows:ServiceReference/>" +
                                "<ows:ServiceReference xlink:href=\"invalid.txt\"/>" +
                                "<ows:ServiceReference xlink:href=\"INVALID.NC\"/>"
                )
        ));

        assertEquals(2, header.getDataFileNames().size());
        assertEquals("valid.nc", header.getDataFileNames().get(0));
        assertEquals("valid.nc.nc", header.getDataFileNames().get(1));
    }

    @Test
    @STTM("SNAP-4126")
    public void testParseVendorSpecificIgnoresEmptyAttribute() throws IOException {
        final FlexProductHeader header = parser.parse(stream(
                xmlBody(
                        "<eop:EarthObservationMetaData>" +
                                "<eop:SpecificInformation>" +
                                "<eop:localAttribute></eop:localAttribute>" +
                                "<eop:localValue>ignored</eop:localValue>" +
                                "</eop:SpecificInformation>" +
                                "<eop:SpecificInformation>" +
                                "<eop:localAttribute>kept</eop:localAttribute>" +
                                "<eop:localValue>value</eop:localValue>" +
                                "</eop:SpecificInformation>" +
                                "</eop:EarthObservationMetaData>"
                )
        ));

        assertEquals(1, header.getVendorSpecific().size());
        assertEquals("value", header.getVendorSpecific().get("kept"));
        assertFalse(header.getVendorSpecific().containsKey(""));
    }

    @Test(expected = IOException.class)
    @STTM("SNAP-4126")
    public void testParseInvalidXmlThrowsIOException() throws IOException {
        parser.parse(stream("<root><broken></root>"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testFlexProductHeaderSettersAndGetters() {
        final FlexProductHeader header = new FlexProductHeader();

        header.setProductName("name");
        header.setProductType("type");
        header.setStartTime("start");
        header.setStopTime("stop");
        header.setPlatformName("platform");
        header.setInstrumentName("instrument");
        header.setOrbitNumber(42);
        header.setOrbitDirection("direction");
        header.setProcessorName("processor");
        header.setProcessorVersion("version");
        header.setDataFileNames(Collections.singletonList("data.nc"));
        header.setVendorSpecific(Collections.singletonMap("key", "value"));

        assertEquals("name", header.getProductName());
        assertEquals("type", header.getProductType());
        assertEquals("start", header.getStartTime());
        assertEquals("stop", header.getStopTime());
        assertEquals("platform", header.getPlatformName());
        assertEquals("instrument", header.getInstrumentName());
        assertEquals(42, header.getOrbitNumber());
        assertEquals("direction", header.getOrbitDirection());
        assertEquals("processor", header.getProcessorName());
        assertEquals("version", header.getProcessorVersion());
        assertEquals(Collections.singletonList("data.nc"), header.getDataFileNames());
        assertEquals(Collections.singletonMap("key", "value"), header.getVendorSpecific());
    }

    private InputStream stream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private String xmlBody(String body) {
        return "<root " +
                "xmlns:gml=\"http://www.opengis.net/gml/3.2\" " +
                "xmlns:eop=\"http://www.opengis.net/eop/2.1\" " +
                "xmlns:ows=\"http://www.opengis.net/ows/2.0\" " +
                "xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                body +
                "</root>";
    }

    private String completeXml() {
        return xmlBody(
                "<gml:TimePeriod>" +
                        "<gml:beginPosition> 2023-07-12T07:59:54.050Z </gml:beginPosition>" +
                        "<gml:endPosition>2023-07-12T08:02:32.346Z</gml:endPosition>" +
                        "</gml:TimePeriod>" +
                        "<eop:Platform><eop:shortName>FLEX</eop:shortName></eop:Platform>" +
                        "<eop:Instrument><eop:shortName>FLORIS</eop:shortName></eop:Instrument>" +
                        "<eop:Acquisition>" +
                        "<eop:orbitNumber> 38179 </eop:orbitNumber>" +
                        "<eop:orbitDirection>DESCENDING</eop:orbitDirection>" +
                        "</eop:Acquisition>" +
                        "<ows:ServiceReference xlink:href=\"./data_1.nc\"/>" +
                        "<ows:ServiceReference xlink:href=\"./data_2.nc.nc\"/>" +
                        "<ows:ServiceReference xlink:href=\"./ignored.txt\"/>" +
                        "<eop:EarthObservationMetaData>" +
                        "<eop:identifier>PRODUCT_NAME</eop:identifier>" +
                        "<eop:productType>L1B_OBS___</eop:productType>" +
                        "<eop:ProcessingInformation>" +
                        "<eop:processorName>L1PF</eop:processorName>" +
                        "<eop:processorVersion>03.02</eop:processorVersion>" +
                        "</eop:ProcessingInformation>" +
                        "<eop:SpecificInformation>" +
                        "<eop:localAttribute>missionPhase</eop:localAttribute>" +
                        "<eop:localValue>COMMISSIONING</eop:localValue>" +
                        "</eop:SpecificInformation>" +
                        "<eop:SpecificInformation>" +
                        "<eop:localAttribute>emptyValue</eop:localAttribute>" +
                        "</eop:SpecificInformation>" +
                        "</eop:EarthObservationMetaData>"
        );
    }
}
