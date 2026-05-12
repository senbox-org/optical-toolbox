package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class FlexL1bReaderPlugInTest {

    private FlexL1bReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new FlexL1bReaderPlugIn();
    }

    @Test
    public void testFormatName() {
        assertEquals("FLEX_L1B", plugIn.getFormatNames()[0]);
    }

    @Test
    public void testFileExtensions() {
        assertArrayEquals(new String[]{".xml"}, plugIn.getDefaultFileExtensions());
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader reader = plugIn.createReaderInstance();
        assertNotNull(reader);
        assertTrue(reader instanceof FlexProductReader);
    }

    @Test
    public void testDecodeQualification_validL1bDirectory() {
        final Path xmlPath = Path.of("FLX_L1B_OBS____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_invalidProduct() {
        final Path xmlPath = Path.of("S3A_OL_1_EFR____20200101T120000", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l1cProduct() {
        final Path xmlPath = Path.of("FLX_L1C_FLXSYN_20230712T075954_20230712T080232", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l2Product() {
        final Path xmlPath = Path.of("FLX_L2__FLXSYN_20230712T075954_20230712T075958", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_caseInsensitive() {
        final Path xmlPath = Path.of("flx_l1b_obs____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testInputTypes() {
        final Class<?>[] inputTypes = plugIn.getInputTypes();
        assertEquals(3, inputTypes.length);
        assertEquals(String.class, inputTypes[0]);
        assertEquals(File.class, inputTypes[1]);
        assertEquals(Path.class, inputTypes[2]);
    }
}
