package eu.esa.opt.dataio.flex;

import org.esa.snap.core.dataio.DecodeQualification;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.Assert.*;


public class FlexL1bReaderPlugInTest {


    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private final FlexL1bReaderPlugIn plugIn = new FlexL1bReaderPlugIn();

    private static final String VALID = "FLX_L1B_OBS____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01";


    @Test
    public void testValidInputsReturnIntended() throws Exception {
        final File dir = createProductDir(VALID);

        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir.toPath()));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(dir.getPath()));
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(new File(dir, VALID + ".xml")));
    }

    @Test
    public void testInvalidInputsReturnUnable() throws Exception {
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(null));
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(new Object()));

        assertUnableForDirectory("FLX_L1C_FLXSYN_20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01");
        assertUnableForDirectory("FLX_L2_FLXSYN_20230712T075954_20230712T080232_20260310T131535_31SDD_100_064_01");
        assertUnableForDirectory("FLX_L1B_WRONG__20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021_01");
        assertUnableForDirectory("FLX_L1B_OBS____20230712T075954_20230712T080232_20260310T131535_0158_100_064_0021");
    }

    @Test
    public void testDirectoryWithoutXmlReturnsUnable() throws Exception {
        final File dir = tempFolder.newFolder(VALID);
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(dir));
    }

    @Test
    public void testNcAndOtherFilesReturnUnable() throws Exception {
        final File dir = createProductDir(VALID);

        assertUnableForFile(dir, VALID + ".nc");
        assertUnableForFile(dir, "OTHER_FILE.NC");
        assertUnableForFile(dir, VALID + ".txt");
    }

    @Test
    public void testXmlWithoutParentOrInvalidParentReturnsUnable() throws Exception {
        final File xmlInRoot = tempFolder.newFile(VALID + ".xml");
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xmlInRoot.toPath().getFileName()));

        final File invalidDir = tempFolder.newFolder("INVALID_PRODUCT");
        final File xml = new File(invalidDir, VALID + ".xml");
        assertTrue(xml.createNewFile());

        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(xml));
    }

    @Test
    public void testMetadataMethods() {
        assertArrayEquals(new String[]{"FLEX_L1B"}, plugIn.getFormatNames());
        assertArrayEquals(new String[]{".xml"}, plugIn.getDefaultFileExtensions());
        assertEquals("FLEX L1B Products (FLORIS TOA radiance)", plugIn.getDescription(Locale.ENGLISH));
        assertArrayEquals(new Class[]{String.class, File.class, Path.class}, plugIn.getInputTypes());
        assertNotNull(plugIn.getProductFileFilter());
        assertNotNull(plugIn.createReaderInstance());
    }

    private File createProductDir(String name) throws Exception {
        final File dir = tempFolder.newFolder(name);
        assertTrue(new File(dir, name + ".xml").createNewFile());
//        assertTrue(new File(dir, name + ".XML").createNewFile());
        return dir;
    }

    private void assertUnableForDirectory(String name) throws Exception {
        final File dir = tempFolder.newFolder(name);
        assertTrue(new File(dir, name + ".xml").createNewFile());
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(dir));
    }

    private void assertUnableForFile(File dir, String name) throws Exception {
        final File file = new File(dir, name);
        assertTrue(file.createNewFile());
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(file));
    }
}
