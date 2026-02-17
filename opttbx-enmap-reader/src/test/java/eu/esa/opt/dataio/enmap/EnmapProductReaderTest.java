package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.awt.Dimension;

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

    @Test
    @STTM("SNAP-4123")
    public void testNormalizeCacheTileDimension_fullSceneTileIsLimitedTo256() {
        Dimension normalized = EnmapProductReader.normalizeCacheTileDimension(new Dimension(1128, 1212), 1128, 1212);

        assertEquals(256, normalized.width);
        assertEquals(256, normalized.height);
    }

    @Test
    @STTM("SNAP-4123")
    public void testNormalizeCacheTileDimension_invalidTileFallsBackToBoundedDefault() {
        Dimension normalized = EnmapProductReader.normalizeCacheTileDimension(new Dimension(1, 0), 200, 180);

        assertEquals(200, normalized.width);
        assertEquals(180, normalized.height);
    }

    @Test
    @STTM("SNAP-4123")
    public void testNormalizeCacheTileDimension_validTileIsKept() {
        Dimension normalized = EnmapProductReader.normalizeCacheTileDimension(new Dimension(128, 64), 1128, 1212);

        assertEquals(128, normalized.width);
        assertEquals(64, normalized.height);
    }
}
