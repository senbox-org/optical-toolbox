package eu.esa.opt.dataio.flex.dddb;

import com.bc.ceres.annotation.STTM;
import org.junit.Test;

import static org.junit.Assert.*;


public class FlexProductDescriptorTest {


    @Test
    @STTM("SNAP-4126")
    public void testDefaultValues() {
        final FlexProductDescriptor descriptor = new FlexProductDescriptor();

        assertEquals("", descriptor.getProductType());
        assertEquals(-1, descriptor.getWidth());
        assertEquals(-1, descriptor.getHeight());
        assertEquals(0, descriptor.getDataFiles().length);
        assertEquals("", descriptor.getBandGroupingPattern());
        assertEquals(0, descriptor.getFlagMasks().length);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSettersAndGetters() {
        final FlexProductDescriptor descriptor = new FlexProductDescriptor();

        descriptor.setProductType("FLX_L1C_FLXSYN");
        descriptor.setWidth(536);
        descriptor.setHeight(3640);
        descriptor.setDataFiles(new String[]{"geometry", "measurement_data"});
        descriptor.setBandGroupingPattern("floris_toa_radiance_ch_*:olci_toa_radiance_ch_*");

        final FlexFlagMask mask = new FlexFlagMask("pixel_classification", "land", 1, "Land pixel", false);
        descriptor.setFlagMasks(new FlexFlagMask[]{mask});

        assertEquals("FLX_L1C_FLXSYN", descriptor.getProductType());
        assertEquals(536, descriptor.getWidth());
        assertEquals(3640, descriptor.getHeight());
        assertArrayEquals(new String[]{"geometry", "measurement_data"}, descriptor.getDataFiles());
        assertEquals("floris_toa_radiance_ch_*:olci_toa_radiance_ch_*", descriptor.getBandGroupingPattern());
        assertEquals(1, descriptor.getFlagMasks().length);
        assertEquals("land", descriptor.getFlagMasks()[0].getName());
    }
}
