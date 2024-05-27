package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SlstrLevel1B500mProductReaderPlugInTest {

    private SlstrLevel1B500mProductReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new SlstrLevel1B500mProductReaderPlugIn();
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = plugIn.getDefaultFileExtensions();
        assertEquals(2, defaultFileExtensions.length);
        assertEquals(".xml", defaultFileExtensions[0]);
        assertEquals(".zip", defaultFileExtensions[1]);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertNotNull(productFileFilter);

        assertEquals("Sen3_SLSTRL1B_500m", productFileFilter.getFormatName());
        final String[] extensions = productFileFilter.getExtensions();
        assertEquals(2, extensions.length);
        assertEquals(".xml", extensions[0]);
        assertEquals(".zip", extensions[1]);
        assertEquals("Sentinel-3 SLSTR L1B products in 500 m resolution (*.xml,*.zip)", productFileFilter.getDescription());
    }
}
