package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class EnmapProductReaderPlugInTest {

    private EnmapProductReaderPlugIn plugIn;
    private String sep;

    private static void assertFormatName(String formatName) {
        assertEquals("EnMAP L1B/L1C/L2A", formatName);
    }

    @Before
    public void setUp() {
        plugIn = new EnmapProductReaderPlugIn();
        sep = File.separator;
    }

    @Test
    public void testGetDecodeQualification_l1b_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L1B_gtif_qualification.zip")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputZip));
    }

    @Test
    public void testGetDecodeQualification_l1c_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L1C_gtif_qualification.zip")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputZip));
    }

    @Test
    public void testGetDecodeQualification_l2a_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification.zip")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputZip));
    }

    @Test
    @STTM("SNAP-3627")
    public void testGetDecodeQualification_l1c_gtif_tar() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("dims_op_oc_oc-en_701293165_1.tar.gz")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputZip));
    }

    @Test
    public void testGetDecodeQualification_l1b_gtif_folder() throws URISyntaxException {
        File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L1B_gtif_qualification/ENMAP01-____L1B-DT0000326721_20170626T102020Z_001_V000204_20200406T154119Z-METADATA.XML")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputFolder));
    }

    @Test
    public void testGetDecodeQualification_l1c_gtif_folder() throws URISyntaxException {
        File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L1C_gtif_qualification/ENMAP01-____L1C-DT0000326721_20170626T102020Z_001_V000204_20200406T180016Z-METADATA.XML")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputFolder));
    }

    @Test
    public void testGetDecodeQualification_l2a_gtif_folder() throws URISyntaxException {
        File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification/ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-METADATA.XML")).toURI());
        assertEquals(DecodeQualification.INTENDED, plugIn.getDecodeQualification(inputFolder));
    }

    @Test
    public void testGetDecodeQualification_invalid() {
        assertEquals(DecodeQualification.UNABLE, plugIn.getDecodeQualification(new int[3]));
    }

    // @todo 2 tb/tb add test with - non-intended decode qualification tb 2024-03-07

    @Test
    public void testConvertToPath() {
        Path input = Paths.get("test");
        Path path = EnmapProductReaderPlugIn.convertToPath(input);
        assertEquals(input.toString(), path.toString());
    }

    @Test
    public void testConvertToPath_invalidInput() {
        assertNull(EnmapProductReaderPlugIn.convertToPath(new int[2]));
    }

    @Test
    public void testGetInputTypes() {
        assertArrayEquals(InputTypes.getTypes(), plugIn.getInputTypes());
    }

    @Test
    public void testGetFormatNames() {
        assertEquals(1, plugIn.getFormatNames().length);
        assertFormatName(plugIn.getFormatNames()[0]);
    }

    @Test
    @STTM("SNAP-3627")
    public void testGetDefaultFileExtensions() {
        assertExtensions(plugIn.getDefaultFileExtensions());
    }

    @Test
    public void testGetDescription() {
        assertDescription(plugIn.getDescription(null));
    }

    @Test
    public void testGetProductFileFilter() {
        SnapFileFilter fileFilter = plugIn.getProductFileFilter();
        assertDescription(fileFilter.getDescription());
        assertExtensions(fileFilter.getExtensions());
        assertFormatName(fileFilter.getFormatName());
    }

    @Test
    public void testExtractPathsFromZip() throws URISyntaxException, IOException {
        final File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification.zip")).toURI());

        final List<Path> paths = EnmapProductReaderPlugIn.extractPathsFromZip(inputZip.toPath());
        assertEquals(12, paths.size());
        assertEquals("enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-METADATA.XML", paths.get(0).toString());
        assertEquals("enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-QL_QUALITY_CLOUD.TIF", paths.get(4).toString());
        assertEquals("enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-QL_QUALITY_TESTFLAGS.TIF", paths.get(8).toString());
    }

    @Test
    public void testExtractFromDir() throws IOException, URISyntaxException {
        final File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification/ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-METADATA.XML")).toURI());

        final List<Path> paths = EnmapProductReaderPlugIn.extractPathsFromDir(inputFolder.toPath());
        assertEquals(12, paths.size());
        assertAnyPathContains(paths, "enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-QL_QUALITY_CIRRUS.TIF");
        assertAnyPathContains(paths, "enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-QL_QUALITY_HAZE.TIF");
        assertAnyPathContains(paths, "enmap_L2A_gtif_qualification" + sep + "ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-QL_VNIR.TIF");
    }

    private void assertAnyPathContains(List<Path> paths, String pathSegment) {
        for (final Path path : paths) {
            if (path.toString().contains(pathSegment)) {
                return;
            }
        }
        fail("Path segment: " + pathSegment + " not in list");
    }

    @Test
    public void testCreateReaderInstance() {
        final ProductReader reader = plugIn.createReaderInstance();
        assertTrue(reader instanceof EnmapProductReader);

        assertEquals(plugIn, reader.getReaderPlugIn());
    }

    @Test
    @STTM("SNAP-3627")
    public void testIsValidEnmapZipName() {
        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("ENMAP01-____L1B-DT0000025905_20230707T192008Z_001_V010303_20230922T131734Z.ZIP"));
        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("dims_op_oc_oc-en_700949147_3\\ENMAP.HSI.L1B\\ENMAP-HSI-L1BDT0000002446_02-2022-08-10T11:24:29.404_schwipe-charter_700949145_722423507_2023-09-22T21:24:00\\ENMAP01-____L1B-DT0000002446_20220810T112429Z_002_V010303_20230922T131816Z.ZIP"));

        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("ENMAP01-____L1C-DT0000063762_20240303T180131Z_001_V010401_20240305T120002Z.ZIP"));
        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("ENMAP-HSI-L1CDT0000063762_01-2024-03-03T18_01_31.002_tomblock-cat1distributor_701293163_759889783_2024-03-05T14_/ENMAP01-____L1C-DT0000063762_20240303T180131Z_001_V010401_20240305T120002Z.ZIP"));

        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("ENMAP01-____L2A-DT0000001049_20220612T105735Z_028_V010303_20230922T131826Z.ZIP"));
        assertTrue(EnmapProductReaderPlugIn.isValidEnmapName("dims_op_oc_oc-en_700949147_4\\ENMAP.HSI.L2A\\ENMAP-HSI-L2ADT0000001049_28-2022-06-12T10_57_35.160_schwipe-charter_700949145_722423345_2023-09-22T19_27_20\\ENMAP01-____L2A-DT0000001049_20220612T105735Z_028_V010303_20230922T131826Z.ZIP"));
    }

    private static void assertExtensions(String[] fileExtensions) {
        assertEquals(4, fileExtensions.length);
        assertEquals(".zip", fileExtensions[0]);
        assertEquals(".xml", fileExtensions[1]);
        assertEquals(".tar.gz", fileExtensions[2]);
        assertEquals(".tgz", fileExtensions[3]);
    }

    private static void assertDescription(String description) {
        assertTrue(description.startsWith("EnMAP L1B/L1C/L2A Product Reader"));
    }
}