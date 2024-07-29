package eu.esa.opt.dataio.enmap;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnmapProductReaderTest {

    @Test
    public void testGetEPSGCode() throws Exception {
        assertEquals("EPSG:32608", EnmapProductReader.getEPSGCode("UTM_Zone8_North"));
        assertEquals("EPSG:32708", EnmapProductReader.getEPSGCode("UTM_Zone08_South"));
        assertEquals("EPSG:32632", EnmapProductReader.getEPSGCode("UTM_Zone32_North"));
        assertEquals("EPSG:32714", EnmapProductReader.getEPSGCode("UTM_Zone14_South"));
        assertEquals("EPSG:3035", EnmapProductReader.getEPSGCode("LAEA-ETRS89"));
        assertEquals("EPSG:4326", EnmapProductReader.getEPSGCode("Geographic"));
    }

    @Test
    public void testGetEPSGCode_invalidInput() throws Exception {
        try {
            EnmapProductReader.getEPSGCode("Heffalump");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
        }
    }
}
