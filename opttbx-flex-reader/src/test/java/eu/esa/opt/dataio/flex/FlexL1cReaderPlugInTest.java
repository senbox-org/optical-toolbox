package eu.esa.opt.dataio.flex;

import com.bc.ceres.annotation.STTM;
import com.bc.ceres.core.runtime.internal.IOHelper;
import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


public class FlexL1cReaderPlugInTest {


    public Path tempFolder = IOHelper.createATempDir();

    private final FlexL1cReaderPlugIn plugIn = new FlexL1cReaderPlugIn();

    private static final String VALID = "FLX_L1C_FLXSYN_20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01";


    @Test
    @STTM("SNAP-4126")
    public void testValidInputsReturnIntended() throws Exception {
        final Path dir = createProductDir(VALID);

        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir.resolve(VALID + ".xml")));
    }

    @Test
    @STTM("SNAP-4126")
    public void testInvalidInputsReturnUnable() throws Exception {
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(null));
        assertThrows(IllegalArgumentException.class, () -> plugIn.getDecodeQualification(new Object()));

        assertUnableForDirectory("FLX_L1B_OBS____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01");
        assertUnableForDirectory("FLX_L2_FLXSYN_20230712T075954_20230712T080232_20260310T131535_31SDD_100_064_01");
        assertUnableForDirectory("FLX_L1C_WRONG__20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01");
        assertUnableForDirectory("FLX_L1C_FLXSYN_20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021");
    }

    @Test
    @STTM("SNAP-4126")
    public void testDirectoryWithoutXmlReturnsUnable() throws Exception {
        final Path dir = tempFolder.resolve(VALID);
        IOHelper.createDirectory(dir);
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(dir));
    }

    @Test
    @STTM("SNAP-4126")
    public void testNcAndOtherPathsReturnUnable() throws Exception {
        final Path dir = createProductDir(VALID);

        assertUnableForPath(dir, VALID + ".nc");
        assertUnableForPath(dir, "OTHER_File.NC");
        assertUnableForPath(dir, VALID + ".txt");
    }

    @Test
    @STTM("SNAP-4126")
    public void testXmlWithoutParentOrInvalidParentReturnsUnable() throws Exception {
        final Path xmlInRoot = tempFolder.resolve(VALID + ".xml");
        IOHelper.createFile(xmlInRoot);
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlInRoot.toString()));

        final Path invalidDir = tempFolder.resolve("INVALID_PRODUCT");
        IOHelper.createDirectory(invalidDir);
        final Path xml = invalidDir.resolve(VALID + ".xml");
        assertTrue(IOHelper.createFile(xml));

        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xml));
    }

    @Test
    @STTM("SNAP-4126")
    public void testMetadataMethods() {
        assertArrayEquals(new String[]{"FLEX_L1C"}, plugIn.getFormatNames());
        assertArrayEquals(new String[]{".xml"}, plugIn.getDefaultFileExtensions());
        assertEquals("FLEX L1C Products (geo-registered TOA radiance)", plugIn.getDescription(Locale.ENGLISH));
        assertArrayEquals(new Class[]{String.class, Path.class, Path.class}, plugIn.getInputTypes());
        assertNotNull(plugIn.getProductFileFilter());
        assertNotNull(plugIn.createReaderInstance());
    }

    @Test
    @STTM("SNAP-4126")
    public void testUpperCaseXmlReturnsIntended() throws Exception {
        final Path dir = tempFolder.resolve(VALID);
        IOHelper.createDirectory(dir);
        final Path xml = dir.resolve("HEADER.XML");
        assertTrue(IOHelper.createFile(xml));

        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(xml));
    }


    private Path createProductDir(String name) throws Exception {
        final Path dir = tempFolder.resolve(name);
        IOHelper.createDirectory(dir);
        final Path xml = dir.resolve(name + ".xml");
        assertTrue(IOHelper.createFile(xml));
        return dir;
    }

    private void assertUnableForDirectory(String name) throws Exception {
        final Path dir = tempFolder.resolve(name);
        IOHelper.createDirectory(dir);
        final Path xml = dir.resolve(name + ".xml");
        assertTrue(IOHelper.createFile(xml));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(dir));
    }

    private void assertUnableForPath(Path dir, String name) throws Exception {
        final Path file = dir.resolve(name);
        assertTrue(IOHelper.createFile(file));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(file));
    }
}
