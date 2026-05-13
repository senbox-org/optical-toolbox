package eu.esa.opt.dataio.flex;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


public class FlexProductReaderTest {


    @Test
    @STTM("SNAP-4126")
    public void testMapProductType_L1B() {
        assertEquals("FLX_L1B_OBS", FlexProductReader.mapProductType("L1B_OBS___"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testMapProductType_L1C() {
        assertEquals("FLX_L1C_FLXSYN", FlexProductReader.mapProductType("L1C_FLXSYN"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testMapProductType_L2() {
        assertEquals("FLX_L2_FLXSYN", FlexProductReader.mapProductType("L2__FLXSYN"));
    }

    @Test(expected = IllegalArgumentException.class)
    @STTM("SNAP-4126")
    public void testMapProductType_unknown() {
        FlexProductReader.mapProductType("UNKNOWN_TYPE");
    }

    @Test
    @STTM("SNAP-4126")
    public void testMapProductType_withFLXPrefix() {
        assertEquals("FLX_L1B_OBS", FlexProductReader.mapProductType("FLX_L1B_OBS____"));
        assertEquals("FLX_L1C_FLXSYN", FlexProductReader.mapProductType("FLX_L1C_FLXSYN_"));
        assertEquals("FLX_L2_FLXSYN", FlexProductReader.mapProductType("FLX_L2__FLXSYN_"));
    }

    @Test
    @STTM("SNAP-4126")
    public void testConstructor() {
        final FlexL1bReaderPlugIn plugIn = new FlexL1bReaderPlugIn();
        final FlexProductReader reader = new FlexProductReader(plugIn);

        assertNotNull(reader);
        assertTrue(reader.isSubsetReadingFullySupported());
    }

    @Test
    @STTM("SNAP-4126")
    public void testClose_canBeCalledMultipleTimes() throws IOException {
        final FlexL1bReaderPlugIn plugIn = new FlexL1bReaderPlugIn();
        final FlexProductReader reader = new FlexProductReader(plugIn);

        reader.close();
        reader.close();
    }
}
