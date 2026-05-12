package eu.esa.opt.dataio.flex.header;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FlexHeaderParserTest {

    private FlexHeaderParser parser;

    @Before
    public void setUp() {
        parser = new FlexHeaderParser();
    }

    @Test
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
    public void testParseL2Header_processingInfo() throws IOException {
        final FlexProductHeader header;
        try (InputStream is = getClass().getResourceAsStream("/flex_l2_header.xml")) {
            header = parser.parse(is);
        }

        assertFalse(header.getProcessorName().isEmpty());
        assertFalse(header.getProcessorVersion().isEmpty());
    }
}
