package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

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

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInputFileName() {
        assertTrue(plugIn.isValidInputFileName("xfdumanifest.xml"));
        assertTrue(plugIn.isValidInputFileName("L1c_Manifest.xml"));
        assertTrue(plugIn.isValidInputFileName("S3A_SL_1_RBT____20130707T153752_20130707T154252_20150217T183530_0299_158_182______SVL_O_NR_001.SEN3.zip"));

        assertFalse(plugIn.isValidInputFileName("manifest.safe"));
        assertFalse(plugIn.isValidInputFileName("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInput() {
        final String sep = File.separator;

        assertTrue(plugIn.isInputValid("S3A_SL_1_RBT____20130707T153752_20130707T154252_20150217T183530_0299_158_182______SVL_O_NR_001.SEN3" + sep + "xfdumanifest.xml"));
        assertTrue(plugIn.isInputValid("S3A_SL_1_RBT____20130707T153752_20130707T154252_20150217T183530_0299_158_182______SVL_O_NR_001.SEN3" + sep + "L1c_Manifest.xml"));
        assertTrue(plugIn.isInputValid("S3A_SL_1_RBT____20130707T153752_20130707T154252_20150217T183530_0299_158_182______SVL_O_NR_001.SEN3.zip"));

        assertFalse(plugIn.isInputValid("S3A_SL_1_RBT____20180809T035343_20180809T035643_20180810T124116_0179_034_218_2520_MAR_O_NT_002.SEN3" + sep + "manifest.safe"));
        assertFalse(plugIn.isInputValid("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }
}
