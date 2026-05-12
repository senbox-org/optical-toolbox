package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class FlexL1cReaderPlugInTest {

    private FlexL1cReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new FlexL1cReaderPlugIn();
    }

    @Test
    public void testFormatName() {
        assertEquals("FLEX_L1C", plugIn.getFormatNames()[0]);
    }

    @Test
    public void testDecodeQualification_validL1cDirectory() {
        final Path xmlPath = Path.of("FLX_L1C_FLXSYN_20230712T075954_20230712T080232_20260311T160418_0158_100_064_0021_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l1bProduct() {
        final Path xmlPath = Path.of("FLX_L1B_OBS____20230712T075954_20230712T080232", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l2Product() {
        final Path xmlPath = Path.of("FLX_L2__FLXSYN_20230712T075954_20230712T075958", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_caseInsensitive() {
        final Path xmlPath = Path.of("flx_l1c_flxsyn_20230712T075954_20230712T080232_20260311T160418_0158_100_064_0021_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }
}
