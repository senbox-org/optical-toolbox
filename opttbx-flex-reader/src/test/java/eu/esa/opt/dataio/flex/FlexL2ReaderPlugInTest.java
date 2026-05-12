package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class FlexL2ReaderPlugInTest {

    private FlexL2ReaderPlugIn plugIn;

    @Before
    public void setUp() {
        plugIn = new FlexL2ReaderPlugIn();
    }

    @Test
    public void testFormatName() {
        assertEquals("FLEX_L2", plugIn.getFormatNames()[0]);
    }

    @Test
    public void testDecodeQualification_validL2Directory() {
        final Path xmlPath = Path.of("FLX_L2__FLXSYN_20230712T075954_20230712T075958_20260326T210006_36TYK_100_064_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l1bProduct() {
        final Path xmlPath = Path.of("FLX_L1B_OBS____20230712T075954_20230712T080232", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_l1cProduct() {
        final Path xmlPath = Path.of("FLX_L1C_FLXSYN_20230712T075954_20230712T080232", "xfdumanifest.xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlPath));
    }

    @Test
    public void testDecodeQualification_caseInsensitive() {
        final Path xmlPath = Path.of("flx_l2__flxsyn_20230712T075954_20230712T075958_20260326T210006_36TYK_100_064_01", "xfdumanifest.xml");
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xmlPath));
    }
}
