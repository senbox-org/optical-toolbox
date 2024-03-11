package eu.esa.opt.dataio.enmap;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.dataio.DecodeQualification;
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
    @STTM("SNAP-3627")
    public void testExtractPathsFromTar() throws URISyntaxException, IOException {
        final File inputTar = new File(Objects.requireNonNull(getClass().getResource("dims_op_oc_oc-en_701293165_1.tar.gz")).toURI());

        final List<Path> paths = EnmapProductReaderPlugIn.extractPathsFromTar(inputTar.toPath());
        assertEquals(15, paths.size());
        assertEquals("enmap_L1B_gtif_qualification" + sep + "ENMAP01-____L1B-DT0000326721_20170626T102020Z_001_V000204_20200406T154119Z-QL_PIXELMASK_SWIR.TIF", paths.get(1).toString());
        assertEquals("enmap_L1B_gtif_qualification" + sep + "ENMAP01-____L1B-DT0000326721_20170626T102020Z_001_V000204_20200406T154119Z-QL_QUALITY_CLOUD.TIF", paths.get(5).toString());
        assertEquals("enmap_L1B_gtif_qualification" + sep + "ENMAP01-____L1B-DT0000326721_20170626T102020Z_001_V000204_20200406T154119Z-QL_QUALITY_TESTFLAGS_SWIR.TIF", paths.get(9).toString());
    }

    private static void assertExtensions(String[] fileExtensions) {
        assertEquals(3, fileExtensions.length);
        assertEquals(".zip", fileExtensions[0]);
        assertEquals(".xml", fileExtensions[1]);
        assertEquals(".tar.gz", fileExtensions[2]);
    }

    private static void assertDescription(String description) {
        assertTrue(description.startsWith("EnMAP L1B/L1C/L2A Product Reader"));
    }
}