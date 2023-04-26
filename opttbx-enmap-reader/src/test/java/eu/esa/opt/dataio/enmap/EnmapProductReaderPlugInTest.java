package eu.esa.opt.dataio.enmap;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.util.io.SnapFileFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.junit.Assert.*;

public class EnmapProductReaderPlugInTest {

    private EnmapProductReaderPlugIn plugIn;

    private static void testFormatName(String formatName) {
        assertEquals("EnMAP L1B/L1C/L2A", formatName);
    }

    @Before
    public void setUp() {
        plugIn = new EnmapProductReaderPlugIn();
    }

    @Test
    public void getDecodeQualification_l1b_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L1B_gtif_qualification.zip")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputZip);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void getDecodeQualification_l1c_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L1C_gtif_qualification.zip")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputZip);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void getDecodeQualification_l2a_gtif_zip() throws URISyntaxException {
        File inputZip = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification.zip")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputZip);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void getDecodeQualification_l1b_gtif_folder() throws URISyntaxException {
        File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L1B_gtif_qualification/ENMAP01-____L1B-DT0000326721_20170626T102020Z_001_V000204_20200406T154119Z-METADATA.XML")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputFolder);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void getDecodeQualification_l1c_gtif_folder() throws URISyntaxException {
        Class<? extends EnmapProductReaderPlugInTest> aClass = getClass();
        File inputFolder = new File(Objects.requireNonNull(aClass.getResource("enmap_L1C_gtif_qualification/ENMAP01-____L1C-DT0000326721_20170626T102020Z_001_V000204_20200406T180016Z-METADATA.XML")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputFolder);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void getDecodeQualification_l2a_gtif_folder() throws URISyntaxException {
        File inputFolder = new File(Objects.requireNonNull(getClass().getResource("enmap_L2A_gtif_qualification/ENMAP01-____L2A-DT0000326721_20170626T102020Z_001_V000204_20200406T201930Z-METADATA.XML")).toURI());
        DecodeQualification dq = plugIn.getDecodeQualification(inputFolder);
        assertEquals(DecodeQualification.INTENDED, dq);
    }

    @Test
    public void createReaderInstance() {
    }

    @Test
    public void getInputTypes() {
        assertArrayEquals(InputTypes.getTypes(), plugIn.getInputTypes());
    }

    @Test
    public void getFormatNames() {
        assertEquals(1, plugIn.getFormatNames().length);
        testFormatName(plugIn.getFormatNames()[0]);
    }

    @Test
    public void getDefaultFileExtensions() {
        testExtensions(plugIn.getDefaultFileExtensions());
    }

    @Test
    public void getDescription() {
        testDescription(plugIn.getDescription(null));
    }

    @Test
    public void getProductFileFilter() {
        SnapFileFilter fileFilter = plugIn.getProductFileFilter();
        testDescription(fileFilter.getDescription());
        testExtensions(fileFilter.getExtensions());
        testFormatName(fileFilter.getFormatName());
    }

    @Test
    public void convertToPath() {
    }

    private void testExtensions(String[] fileExtensions) {
        assertEquals(2, fileExtensions.length);
        assertEquals(".zip", fileExtensions[0]);
        assertEquals(".xml", fileExtensions[1]);
    }

    private void testDescription(String description) {
        assertTrue(description.startsWith("EnMAP L1B/L1C/L2A Product Reader"));
    }

}