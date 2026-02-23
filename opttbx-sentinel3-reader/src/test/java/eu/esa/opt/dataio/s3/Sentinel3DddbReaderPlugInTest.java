package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;

import static eu.esa.opt.dataio.s3.S3ReaderPlugInTest.createManifestFilePath;
import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;

public class Sentinel3DddbReaderPlugInTest {

    private Sentinel3DddbReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new Sentinel3DddbReaderPlugIn();
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_OlciLevel1b() {
        String path = createManifestFilePath("OL", "1", "ERR", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));

        path = createManifestFilePath("OL", "1", "EFR", ".SEN3.zip");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));

        path = "sensors_platforms/SENTINEL-3/olci/L1/S3A_OL_1_EFR____20180407T004637_20180407T004937_20180408T065235_0180_029_373_2520_MAR_O_NT_002.SEN3/xfdumanifest.xml";
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_OlciLevel2L() {
        final String path = createManifestFilePath("OL", "2", "LFR", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testDecodeQualification_OlciLevel2W() {
        final String path = createManifestFilePath("OL", "2", "WFR", ".SEN3");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_SlstrLevel1b() {
        final String path = createManifestFilePath("SL", "1", "RBT", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_SlstrWct() {
        final String path = createManifestFilePath("SL", "2", "WCT", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_SlstrWst() {
        final String path = createManifestFilePath("SL", "2", "WST", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_SlstrLst() {
        final String path = createManifestFilePath("SL", "2", "LST", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_SynergyLevel2() {
        String path = createManifestFilePath("SY", "2", "SYN", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));

        path = "sensors_platforms/SENTINEL-3/synergy/S3A_SY_2_VGP____20160415T110058_20160415T110845_20160502T125459_0466_003_094______LN1_D_NC____.SEN3/xfdumanifest.xml";
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_VgtP() {
        final String path = createManifestFilePath("SY", "2", "VGP", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_VgtS() {
        final String path = createManifestFilePath("SY", "3", "VG1", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

// @todo 2 tb/tb these are not covered by decode qualification test 2025-02-04
// ER1_AT_1_RBT|ER2_AT_1_RBT|ENV_AT_1_RBT

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_WithInvalidDataSource() {
        String invalidPath = createManifestFilePath("SY", "1", "XXX", "NONSENSE");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualificationWith_WrongFile() {
        final String invalidPath = "/S3_SY_2_ERR_TTTTTTTTTTTT_instanceID_GGG_CCCC_VV/no_valid_file.xslt";
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetInputTypes() {
        final Class[] inputTypes = plugIn.getInputTypes();
        assertEquals(2, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testGetFormatNames() {
        final String[] formatNames = plugIn.getFormatNames();
        assertEquals(1, formatNames.length);
        assertEquals("Sen3_DDDB", formatNames[0]);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testGetDefaultFileExtensions() {
        final String[] defaultFileExtensions = plugIn.getDefaultFileExtensions();
        assertEquals(2, defaultFileExtensions.length);
        assertEquals(".xml", defaultFileExtensions[0]);
        assertEquals(".zip", defaultFileExtensions[1]);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testGetDescription() {
        final String description = plugIn.getDescription(null);
        assertEquals("Sentinel-3 products", description);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testGetProductFileFilter() {
        final SnapFileFilter productFileFilter = plugIn.getProductFileFilter();
        assertNotNull(productFileFilter);

        assertEquals("Sen3_DDDB", productFileFilter.getFormatName());
        final String[] extensions = productFileFilter.getExtensions();
        assertEquals(2, extensions.length);
        assertEquals(".xml", extensions[0]);
        assertEquals(".zip", extensions[1]);
        assertEquals("Sentinel-3 products (*.xml,*.zip)", productFileFilter.getDescription());
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testIsValidSourceName() {
        assertTrue(plugIn.isValidSourceName("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3"));
        assertTrue(plugIn.isValidSourceName("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3.zip"));
        assertTrue(plugIn.isValidSourceName("S3B_OL_1_EFR____20231214T092214_20231214T092514_20231214T204604_0179_087_207_2340_PS2_O_NT_003.SEN3"));
        assertTrue(plugIn.isValidSourceName("S3A_OL_2_WRR____20250712T104943_20250712T113404_20251129T124016_2661_128_094______MAR_F_NT_004.SEN3"));
        assertTrue(plugIn.isValidSourceName("S3B_OL_2_WFR____20221216T131616_20221216T131916_20260119T163602_0180_074_038______MAR_F_NT_004.SEN3"));

        assertFalse(plugIn.isValidSourceName("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3.zip"));
        assertFalse(plugIn.isValidSourceName("S2A_OPER_PRD_MSIL1C_PDMC_20160918T063540_R022_V20160916T101022_20160916T101045.SAFE"));
        assertFalse(plugIn.isValidSourceName("SM_OPER_MIR_SCLF1C_20221224T220123_20221224T225442_724_001_1.zip"));
        assertFalse(plugIn.isValidSourceName("S3A_OL_1_EFR____20170101T095821_20170101T100021_20171010T055022_0119_012_350______MR1_R_NT_002.SEN3__calimnos.nc"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testIsValidInputFileName() {
        assertTrue(plugIn.isValidInputFileName("xfdumanifest.xml"));
        assertTrue(plugIn.isValidInputFileName("L1c_Manifest.xml"));
        assertTrue(plugIn.isValidInputFileName("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3"));

        assertFalse(plugIn.isValidInputFileName("S3A_SY_2_VGP____20160415T110058_20160415T110845_20160502T125459_0466_003_094______LN1_D_NC____.SEN3"));
        assertFalse(plugIn.isValidInputFileName("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3.zip"));
        assertFalse(plugIn.isValidInputFileName("manifest.safe"));
        assertFalse(plugIn.isValidInputFileName("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
        assertFalse(plugIn.isValidInputFileName("S3A_OL_1_EFR____20170101T095821_20170101T100021__calimnos.nc"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testIsValidInput() {
        final String sep = File.separator;

        assertTrue(plugIn.isInputValid("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3"));

        assertFalse(plugIn.isInputValid("S3A_SL_1_RBT____20180809T035343_20180809T035643_20180810T124116_0179_034_218_2520_MAR_O_NT_002.SEN3" + sep + "manifest.safe"));
        assertFalse(plugIn.isInputValid("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3" + sep + "xfdumanifest.xml"));
        assertFalse(plugIn.isInputValid("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3" + sep + "L1c_Manifest.xml"));
        assertFalse(plugIn.isInputValid("S3A_OL_2_LFR____20240526T204947_20240526T205247_20240526T225409_0179_112_385_1980_PS1_O_NR_002.SEN3.zip"));
        assertFalse(plugIn.isInputValid("S5P_NRTI_L2__SO2____20240219T082248_20240219T082748_32914_03_020601_20240219T090920.nc"));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testCreateReaderInstanceReturnsNewInstanceEachTime() {
        final ProductReader firstInstance = plugIn.createReaderInstance();
        assertNotNull(firstInstance);
        final ProductReader secondInstance = plugIn.createReaderInstance();
        assertNotSame(secondInstance, firstInstance);
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711,SNAP-4149")
    public void testIfPlugInIsLoaded() {
        final ProductIOPlugInManager ioPlugInManager = ProductIOPlugInManager.getInstance();
        final Iterator<ProductReaderPlugIn> readerPlugIns = ioPlugInManager.getReaderPlugIns("Sen3_DDDB");
        assertTrue(readerPlugIns.hasNext());
        assertTrue(readerPlugIns.next() instanceof Sentinel3DddbReaderPlugIn);
    }
}
