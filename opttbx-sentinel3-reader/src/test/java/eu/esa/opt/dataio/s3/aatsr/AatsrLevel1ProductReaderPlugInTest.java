package eu.esa.opt.dataio.s3.aatsr;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.aatsr.AatsrLevel1ProductReader;
import eu.esa.opt.dataio.s3.aatsr.AatsrLevel1ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.regex.Pattern;

import static eu.esa.opt.dataio.s3.aatsr.AatsrLevel1ProductReaderPlugIn.DIRECTORY_NAME_PATTERN;
import static org.junit.Assert.*;

public class AatsrLevel1ProductReaderPlugInTest {

    private AatsrLevel1ProductReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new AatsrLevel1ProductReaderPlugIn();
    }

    @Test
    public void testGetFormatNames() {
        assertEquals(1, plugIn.getFormatNames().length);
        assertEquals("ATS_L1_S3", plugIn.getFormatNames()[0]);
    }

    @Test
    public void testGetInputTypes() {
        assertEquals(2, plugIn.getInputTypes().length);
        assertEquals(String.class, plugIn.getInputTypes()[0]);
        assertEquals(File.class, plugIn.getInputTypes()[1]);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = plugIn.getDefaultFileExtensions();
        assertEquals(2, defaultFileExtensions.length);
        assertEquals(".xml", defaultFileExtensions[0]);
        assertEquals(".ZIP", defaultFileExtensions[1]);
    }

    @Test
    @STTM("SNAP-3666")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertNotNull(productFileFilter);

        assertEquals("ATS_L1_S3", productFileFilter.getFormatName());
        final String[] extensions = productFileFilter.getExtensions();
        assertEquals(2, extensions.length);
        assertEquals(".xml", extensions[0]);
        assertEquals(".ZIP", extensions[1]);
        assertEquals("(A)ATSR Level 1 in Sentinel-3 product format (*.xml,*.ZIP)", productFileFilter.getDescription());
    }

    @Test
    public void testCreateReaderInstance() {
        assertEquals(AatsrLevel1ProductReader.class, plugIn.createReaderInstance().getClass());
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidSourceName() {
        assertTrue(plugIn.isValidSourceName("ENV_AT_1_RBT____20110107T111532_20110107T130045_20180928T095029_6312_098_166______TPZ_R_NT_004.SEN3"));
        assertTrue(plugIn.isValidSourceName("ER1_AT_1_RBT____19910901T061936_19910901T080223_20180928T124040_6167_014_005______TPZ_R_NT_004.SEN3"));
        assertTrue(plugIn.isValidSourceName("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3.ZIP"));

        assertFalse(plugIn.isValidSourceName("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3"));
        assertFalse(plugIn.isValidSourceName("SM_OPER_MIR_SCLF1C_20221224T220123_20221224T225442_724_001_1.zip"));
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInputFileName() {
        assertTrue(plugIn.isValidInputFileName("xfdumanifest.xml"));
        assertTrue(plugIn.isValidInputFileName("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3.ZIP"));

        // no alternative manifest name defined 2024-05-28 tb
        assertFalse(plugIn.isValidInputFileName("L1c_Manifest.xml"));
        assertFalse(plugIn.isValidInputFileName("manifest.safe"));
        assertFalse(plugIn.isValidInputFileName("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }

    @Test
    @STTM("SNAP-3666")
    public void testIsValidInput() {
        final String sep = File.separator;

        assertTrue(plugIn.isInputValid("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3.ZIP" + sep + "xfdumanifest.xml"));
        assertTrue(plugIn.isInputValid("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3.ZIP"));

        // no alternative manifest name defined 2024-05-28 tb
        assertFalse(plugIn.isInputValid("ENV_AT_1_RBT____20120316T160758_20120316T175312_20210818T165348_6313_112_370______DSI_R_NT_004.SEN3" + sep + "L1c_Manifest.xml"));
        assertFalse(plugIn.isInputValid("S3A_SL_1_RBT____20180809T035343_20180809T035643_20180810T124116_0179_034_218_2520_MAR_O_NT_002.SEN3" + sep + "manifest.safe"));
        assertFalse(plugIn.isInputValid("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }
}