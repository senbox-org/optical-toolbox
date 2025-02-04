package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class Sentinel3Level1ReaderPlugInTest {

    private Sentinel3Level1ReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new Sentinel3Level1ReaderPlugIn();
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_OlciLevel1b() {
        String path = createManifestFilePath("OL", "1", "ERR", "");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));

        path = createManifestFilePath("OL", "1", "EFR", "");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_OlciLevel2L() {
        final String path = createManifestFilePath("OL", "2", "LFR", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
    }

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_OlciLevel2W() {
        final String path = createManifestFilePath("OL", "2", "WFR", ".SEN3");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(path));
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

// @todo 2 tb/tb these are not vovered by decode qualification test 2025-02-04
// ER1_AT_1_RBT|ER2_AT_1_RBT|ENV_AT_1_RBT

    @Test
    @STTM("SNAP-1696,SNAP-3711")
    public void testDecodeQualification_WithInvalidDataSource() {
        String invalidPath = createManifestFilePath("SY", "1", "XXX", "NONSENSE");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(invalidPath));
    }

    // @todo 1 tb/tb duplicated code from other plugin - refactor and add common utility 2025-02-04
    private static String createManifestFilePath(String sensorId, String levelId, String productId, String suffix) {
        String validParentDirectory = String.format("S3_%s_%s_%s_TTTTTTTTTTTT_%s" , sensorId,
                levelId, productId, suffix);
        String manifestFile = "xfdumanifest.xml";
        return validParentDirectory +  File.separator + manifestFile;
    }
}
