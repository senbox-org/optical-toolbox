package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class L1BPaceOciFileReaderTest {

    @Test
    @STTM("SNAP-4170")
    public void testGetWvlType() {
        assertEquals(L1BPaceOciFileReader.WvlType.BLUE, L1BPaceOciFileReader.getWvlType("Lt_blue"));
        assertEquals(L1BPaceOciFileReader.WvlType.BLUE, L1BPaceOciFileReader.getWvlType("rhot_blue"));
        assertEquals(L1BPaceOciFileReader.WvlType.BLUE, L1BPaceOciFileReader.getWvlType("qual_blue"));

        assertEquals(L1BPaceOciFileReader.WvlType.RED, L1BPaceOciFileReader.getWvlType("Lt_red"));
        assertEquals(L1BPaceOciFileReader.WvlType.RED, L1BPaceOciFileReader.getWvlType("rhot_red"));
        assertEquals(L1BPaceOciFileReader.WvlType.RED, L1BPaceOciFileReader.getWvlType("qual_red"));

        assertEquals(L1BPaceOciFileReader.WvlType.SWIR, L1BPaceOciFileReader.getWvlType("Lt_SWIR"));
        assertEquals(L1BPaceOciFileReader.WvlType.SWIR, L1BPaceOciFileReader.getWvlType("rhot_SWIR"));
        assertEquals(L1BPaceOciFileReader.WvlType.SWIR, L1BPaceOciFileReader.getWvlType("qual_SWIR"));

        assertNull(L1BPaceOciFileReader.getWvlType("heffalump"));
    }

    @Test
    @STTM("SNAP-4170")
    public void testGetLayerBandName() {
        assertEquals("band_234.67", L1BPaceOciFileReader.getLayerBandName("band", 234.67f));
        assertEquals("wurst_11.8", L1BPaceOciFileReader.getLayerBandName("wurst", 11.8f));
    }

    @Test
    @STTM("SNAP-4170")
    public void testRemoveWvlFromName() {
        assertEquals("band", L1BPaceOciFileReader.removeWvlFromName("band_234.67"));
        assertEquals("rhot_red", L1BPaceOciFileReader.removeWvlFromName("rhot_red_642.228"));

        assertEquals("latitude", L1BPaceOciFileReader.removeWvlFromName("latitude"));
    }
}
