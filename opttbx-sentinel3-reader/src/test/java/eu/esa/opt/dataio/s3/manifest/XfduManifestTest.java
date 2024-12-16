package eu.esa.opt.dataio.s3.manifest;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Tonio Fincke
 */
public class XfduManifestTest {

    private static Manifest slstr_manifest = null;
    private static Manifest olci_manifest = null;

    @BeforeClass
    public static void setUp() throws ParserConfigurationException, IOException, SAXException {
        try (final InputStream stream = XfduManifestTest.class.getResourceAsStream("slstr_xfdumanifest.xml")) {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            slstr_manifest = XfduManifest.createManifest(doc);
        }

        try (final InputStream stream = XfduManifestTest.class.getResourceAsStream("olci_xfdumanifest.xml")) {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            olci_manifest = XfduManifest.createManifest(doc);
        }
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetProductname() throws Exception {
        assertEquals("S3B_SL_1_RBT____20241113T081123_20241113T081423_20241113T095357_0179_099_363_3240_PS2_O_NR_004.SEN3", slstr_manifest.getProductName());
        assertEquals("S3A_OL_1_EFR____20240526T155849_20240526T160149_20240526T174356_0179_112_382_2700_PS1_O_NR_004.SEN3", olci_manifest.getProductName());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetProductType() throws Exception {
        assertEquals("SL_1_RBT", slstr_manifest.getProductType());
        assertEquals("OL_1_EFR", olci_manifest.getProductType());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetbaselineCollection() {
        assertEquals("004", olci_manifest.getBaselineCollection());
        assertEquals("004", slstr_manifest.getBaselineCollection());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetDescription() throws Exception {
        assertEquals("SENTINEL-3 SLSTR Level 1 package", slstr_manifest.getDescription());
        assertEquals("SENTINEL-3 OLCI Level 1 Earth Observation Full Resolution Product", olci_manifest.getDescription());
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetStartTime() throws Exception {
        final ProductData.UTC expected_slstr = ProductData.UTC.parse("2024-11-13T08:11:22.850867", "yyyy-MM-dd'T'HH:mm:ss");
        ProductData.UTC startTime = slstr_manifest.getStartTime();
        assertTrue(expected_slstr.equalElems(startTime));

        final ProductData.UTC expected_olci = ProductData.UTC.parse("2024-05-26T15:58:49.283029", "yyyy-MM-dd'T'HH:mm:ss");
        startTime = olci_manifest.getStartTime();
        assertTrue(expected_olci.equalElems(startTime));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetStopTime() throws Exception {
        final ProductData.UTC expected_slstr = ProductData.UTC.parse("2024-11-13T08:14:22.850867", "yyyy-MM-dd'T'HH:mm:ss");
        ProductData.UTC stopTime = slstr_manifest.getStopTime();
        assertTrue(expected_slstr.equalElems(stopTime));

        final ProductData.UTC expected_olci = ProductData.UTC.parse("2024-05-26T16:01:49.283029", "yyyy-MM-dd'T'HH:mm:ss");
        stopTime = olci_manifest.getStopTime();
        assertTrue(expected_olci.equalElems(stopTime));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetFileNames() {
        final String[] excluded = new String[0];

        final List<String> slstrFileNames = slstr_manifest.getFileNames(excluded);
        assertEquals(97, slstrFileNames.size());
        assertEquals("viscal.nc", slstrFileNames.get(0));
        assertEquals("flags_fn.nc", slstrFileNames.get(22));
        assertEquals("met_tx.nc", slstrFileNames.get(45));
        assertEquals("S4_quality_bo.nc", slstrFileNames.get(61));
        assertEquals("S7_BT_io.nc", slstrFileNames.get(83));

        final List<String> olciFileNames = olci_manifest.getFileNames(excluded);
        assertEquals(50, olciFileNames.size());
        assertEquals("Oa01_radiance.nc", olciFileNames.get(0));
        assertEquals("Oa06_radiance_unc.nc", olciFileNames.get(11));
        assertEquals("Oa14_radiance_unc.nc", olciFileNames.get(27));
        assertEquals("Oa20_radiance.nc", olciFileNames.get(38));
        assertEquals("geo_coordinates.nc", olciFileNames.get(42));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetFileNames_Exclusions() {
        final String[] excluded = {"removedPixelsData"};
        final List<String> fileNames = olci_manifest.getFileNames(excluded);
        assertEquals(49, fileNames.size());

        assertFalse(fileNames.contains("removed_pixels.nc"));
        assertEquals("Oa03_radiance_unc.nc", fileNames.get(5));
        assertEquals("Oa10_radiance.nc", fileNames.get(18));
        assertEquals("Oa14_radiance_unc.nc", fileNames.get(27));
    }

    @Test
    @STTM("SNAP-3711")
    public void testRemoveUnderbarsAtEnd() {
        assertEquals("bibo", XfduManifest.removeUnderbarsAtEnd("bibo_"));
        assertEquals("OL_1_EFR", XfduManifest.removeUnderbarsAtEnd("OL_1_EFR___"));

        assertEquals("Heffalump", XfduManifest.removeUnderbarsAtEnd("Heffalump"));
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetXPathString() {
        String xpathValue = slstr_manifest.getXPathString("/XFDU/metadataSection/metadataObject[@ID='slstrProductInformation']//metadataWrap/xmlData/slstrProductInformation/nadirImageSize[@grid=\"0.5 km stripe A\"]/rows");
        assertEquals("2400", xpathValue);

        xpathValue = olci_manifest.getXPathString("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/rows");
        assertEquals("4091", xpathValue);
    }

    @Test
    @STTM("SNAP-3711")
    public void testGetXPathInt() {
        int xpathValue = slstr_manifest.getXPathInt("/XFDU/metadataSection/metadataObject[@ID='slstrProductInformation']//metadataWrap/xmlData/slstrProductInformation/nadirImageSize[@grid=\"0.5 km stripe A\"]/columns");
        assertEquals(3000, xpathValue);

        xpathValue = olci_manifest.getXPathInt("/XFDU/metadataSection/metadataObject[@ID='olciProductInformation']//metadataWrap/xmlData/olciProductInformation/imageSize/columns");
        assertEquals(4865, xpathValue);
    }
}
