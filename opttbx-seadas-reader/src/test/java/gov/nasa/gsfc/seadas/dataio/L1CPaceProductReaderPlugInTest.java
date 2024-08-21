package gov.nasa.gsfc.seadas.dataio;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class L1CPaceProductReaderPlugInTest {

    private ProductReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new L1CPaceProductReaderPlugIn();
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684")
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();

        assertEquals(2, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684")
    public void testCreateReaderInstance() {
        final ProductReader reader = plugIn.createReaderInstance();

        assertTrue(reader instanceof SeadasProductReader);
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684,SNAP-3808")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();

        assertEquals("PaceOCI_L1C", productFileFilter.getFormatName());
        assertEquals(".nc", productFileFilter.getDefaultExtension());
        assertEquals("PACE OCI L1C Products (*.nc)", productFileFilter.getDescription());
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684")
    public void testGetDefaultFileExtensions() {
        final String[] extensions = plugIn.getDefaultFileExtensions();

        assertEquals(1, extensions.length);
        assertEquals(".nc", extensions[0]);
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684")
    public void testGetDescription() {
        final String description = plugIn.getDescription(null);// no locale requires here tb 2024-05-15

        assertEquals("PACE OCI L1C Products", description);
    }

    @Test
    @STTM("SNAP-3683,SNAP-3684,SNAP-3808")
    public void testGetFormatNames() {
        final String[] formatNames = plugIn.getFormatNames();

        assertEquals(1, formatNames.length);
        assertEquals("PaceOCI_L1C", formatNames[0]);
    }
}
