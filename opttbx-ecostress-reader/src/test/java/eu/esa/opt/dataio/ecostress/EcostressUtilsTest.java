package eu.esa.opt.dataio.ecostress;

import eu.esa.snap.hdf.HDFLoader;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author adraghici
 */
public class EcostressUtilsTest {

    private static final String ECOSTRESS_TEST_PRODUCT_FILE_NAME = "ECOSTRESS_L2_CLOUD_32257_019_20240314T205642_0601_01.h5";

    @Before
    public void loadHdf5Library() {
        HDFLoader.ensureHDF5Initialised();
    }

    @Test
    public void ecostressNodesExistsTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                Assert.assertTrue(EcostressUtils.ecostressNodesExists(ecostressFile, EcostressConstants.ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA));
                Assert.assertFalse(EcostressUtils.ecostressNodesExists(ecostressFile, "/other"));
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void extractMetadataElementsTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                final List<MetadataElement> metadataElements = EcostressUtils.extractMetadataElements(ecostressFile, EcostressConstants.ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA);
                Assert.assertFalse(metadataElements.isEmpty());
                final MetadataElement metadataElement = metadataElements.get(0);
                Assert.assertEquals(44, metadataElement.getNumAttributes());
                final MetadataAttribute metadataAttribute = metadataElement.getAttributeAt(19);
                Assert.assertEquals("PGEName", metadataAttribute.getName());
                Assert.assertEquals("L2_PGE", metadataAttribute.getData().getElemString());
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void extractStartTimeTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                Assert.assertEquals("14-MAR-2024 20:56:43.163661", EcostressUtils.extractStartTime(ecostressFile).getElemString());
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void extractEndTimeTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                Assert.assertEquals("14-MAR-2024 21:01:43.163661", EcostressUtils.extractEndTime(ecostressFile).getElemString());
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void extractEcostressProductDimensionTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                final Dimension dimension = EcostressUtils.extractEcostressProductDimension(ecostressFile, EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS, EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_LINES);
                Assert.assertEquals(5400, dimension.width);
                Assert.assertEquals(5632, dimension.height);
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void extractBandsObjectsTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                final List<Band> bands = EcostressUtils.extractBandsObjects(ecostressFile, "/SDS");
                Assert.assertEquals(1, bands.size());
                final Band band = bands.get(0);
                Assert.assertEquals("SDS_CloudMask", band.getName());
                Assert.assertEquals(ProductData.TYPE_INT8, band.getDataType());
                Assert.assertEquals(5400, band.getRasterWidth());
                Assert.assertEquals(5632, band.getRasterHeight());
                Assert.assertEquals(0d, band.getNoDataValue(), 0.1);
                Assert.assertEquals("Bits: 0 = Determined, 1 = Cloud (includes tests and extended), 2 = Test 1 or Test 2, 3 = Test 1: Thermal Brightness Test, 4 = Test 2: Band 4 - 5 Thermal Difference Test, 5 = Water", band.getUnit());
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    @Test
    public void readEcostressBandDataTest() {
        try {
            final Path ecostressTestProductFilePath = getEcostressTestProductFilePath();
            try (final EcostressFile ecostressFile = new EcostressFile(ecostressTestProductFilePath.toFile())) {
                final List<Band> bands = EcostressUtils.extractBandsObjects(ecostressFile, "/SDS");
                Assert.assertEquals(1, bands.size());
                final Band band = bands.get(0);
                final byte[] productDataElements = new byte[100 * 100];
                final ProductData productData = ProductData.createInstance(productDataElements);
                EcostressUtils.readEcostressBandData(ecostressFile, band, 100, 100, 400, 3000, productData);
                Assert.assertEquals(100 * 100, productData.getNumElems());
                Assert.assertEquals(1, productData.getElemIntAt(74 + 31 * 100));
                Assert.assertEquals(33, productData.getElemIntAt(75 + 31 * 100));
            }
        } catch (Exception e) {
            Assert.fail("Test crashed. Reason: " + e.getMessage());
        }
    }

    private static Path getEcostressTestProductFilePath() throws URISyntaxException {
        final URL url = EcostressUtilsTest.class.getResource(ECOSTRESS_TEST_PRODUCT_FILE_NAME);
        if (url != null) {
            return Paths.get(url.toURI());
        }
        throw new IllegalStateException("ECOSTRESS test product file cannot be loaded.");
    }
}
