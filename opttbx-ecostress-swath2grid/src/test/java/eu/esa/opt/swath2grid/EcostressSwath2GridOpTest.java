package eu.esa.opt.swath2grid;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.engine_utilities.utils.TestUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * Test for EcostressSwath2GridOp
 */
public class EcostressSwath2GridOpTest {

    private static final String ECOSTRESS_SWATH2GRID_TESTS_FOLDER = "_ecostress_swath2grid";
    private static final String ECOSTRESS_TEST_PRODUCT_FILE_NAME_ECO = "ECOSTRESS_L1B_RAD_08355_008_20191226T171130_0601_01.h5";
    private static final String ECOSTRESS_TEST_PRODUCT_FILE_NAME_GEO = "ECOSTRESS_L1B_GEO_08355_008_20191226T171130_0601_01.h5";

    protected Path ecostressSwath2GridTestsFolderPath;

    @Before
    public final void setUp() {
        assumeTrue(TestUtil.testdataAvailable());
        checkTestDirectoryExists();
        SystemUtils.initGeoTools();
    }

    private void checkTestDirectoryExists() {
        String testDirectoryPathProperty = System.getProperty(TestUtil.PROPERTYNAME_DATA_DIR);
        assertNotNull("The system property '" + TestUtil.PROPERTYNAME_DATA_DIR + "' representing the test directory is not set.", testDirectoryPathProperty);
        Path testFolderPath = Paths.get(testDirectoryPathProperty);
        if (!Files.exists(testFolderPath)) {
            fail("The test directory path " + testDirectoryPathProperty + " is not valid.");
        }

        this.ecostressSwath2GridTestsFolderPath = testFolderPath.resolve(ECOSTRESS_SWATH2GRID_TESTS_FOLDER);
        if (!Files.exists(ecostressSwath2GridTestsFolderPath)) {
            fail("The Ecostress Swath2Grid test directory path " + ecostressSwath2GridTestsFolderPath.toString() + " is not valid.");
        }
    }

    @Test
    public void testEcostressSwath2GridOp() throws Exception {
        final ProductReaderPlugIn productReaderPlugIn = buildEcostressProductReaderPlugIn();

        final File ecostressGeoProductFile = this.ecostressSwath2GridTestsFolderPath.resolve(ECOSTRESS_TEST_PRODUCT_FILE_NAME_GEO).toFile();
        Product ecostressGeoProduct = productReaderPlugIn.createReaderInstance().readProductNodes(ecostressGeoProductFile, null);

        final File ecostressEcoProductFile = this.ecostressSwath2GridTestsFolderPath.resolve(ECOSTRESS_TEST_PRODUCT_FILE_NAME_ECO).toFile();
        Product ecostressEcoProduct = productReaderPlugIn.createReaderInstance().readProductNodes(ecostressEcoProductFile, null);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("crsIN", "GEO");
        parameters.put("r2b", true);

        final Map<String, Product> sourceProducts = new HashMap<>();
        sourceProducts.put("geoSourceProduct", ecostressGeoProduct);
        sourceProducts.put("ecoSourceProduct", ecostressEcoProduct);

        // create the operator
        final Operator operator = GPF.getDefaultInstance().createOperator("EcostressSwath2GridOp", parameters, sourceProducts, null);

        // execute the operator
        operator.execute(ProgressMonitor.NULL);

        // get the operator target product
        final Product targetProduct = operator.getTargetProduct();

        assertNotNull(targetProduct);

        assertEquals(9586, targetProduct.getSceneRasterWidth());
        assertEquals(8182, targetProduct.getSceneRasterHeight());

        assertEquals(13, targetProduct.getNumBands());

        Band band = targetProduct.getBandAt(7);
        assertNotNull(band);

        assertEquals(ProductData.TYPE_FLOAT32, band.getDataType());

        checkBand(band);
    }

    private static void checkBand(Band band) {
        long size = (long) band.getRasterWidth() * band.getRasterHeight();
        assertEquals(size, band.getNumDataElems());
        System.out.println();
        assertEquals(-9999, band.getSampleFloat(3396, 515),1E-4);
        assertEquals(262.8568, band.getSampleFloat(3956, 515),1E-4);
        assertEquals(262.64368, band.getSampleFloat(3956, 600),1E-4);
        assertEquals(-9997.0, band.getSampleFloat(5263, 670),1E-4);
        assertEquals(-9997.0, band.getSampleFloat(6555, 1411),1E-4);

        assertEquals(256.4883, band.getSampleFloat(2812, 1828),1E-4);
        assertEquals(266.1533, band.getSampleFloat(3316, 1964),1E-4);
        assertEquals(260.0402, band.getSampleFloat(3588, 3068),1E-4);
        assertEquals(270.8196, band.getSampleFloat(5228, 3224),1E-4);
        assertEquals(259.2538, band.getSampleFloat(7444, 2418),1E-4);

        assertEquals(255.4887, band.getSampleFloat(2236, 3668),1E-4);
        assertEquals(264.6780, band.getSampleFloat(3292, 3596),1E-4);
        assertEquals(273.8870, band.getSampleFloat(3876, 3739),1E-4);
        assertEquals(279.9133, band.getSampleFloat(5000, 4000),1E-4);
        assertEquals(279.6192, band.getSampleFloat(5940, 4000),1E-4);

        assertEquals(270.1467, band.getSampleFloat(2940, 4343),1E-4);
        assertEquals(276.1724, band.getSampleFloat(4532, 4474),1E-4);
        assertEquals(283.3075, band.getSampleFloat(4628, 4515),1E-4);
        assertEquals(274.3342, band.getSampleFloat(6148, 4019),1E-4);
        assertEquals(281.1465, band.getSampleFloat(7777, 5252),1E-4);

        assertEquals(281.1253, band.getSampleFloat(2988, 6211),1E-4);
        assertEquals(281.2882, band.getSampleFloat(4488, 5891),1E-4);
        assertEquals(276.9856, band.getSampleFloat(4780, 5829),1E-4);
        assertEquals(271.9381, band.getSampleFloat(4804, 6275),1E-4);
        assertEquals(280.8099, band.getSampleFloat(5445, 8181),1E-4);
    }

    protected static ProductReaderPlugIn buildEcostressProductReaderPlugIn() throws Exception {
        final Class<?> ecostressReaderPlugInClass = Class.forName("eu.esa.opt.dataio.ecostress.EcostressProductReaderPlugIn");
        return (ProductReaderPlugIn)ecostressReaderPlugInClass.getDeclaredConstructor().newInstance();
    }
}
