package eu.esa.opt.olci.l1csyn;

import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class L1cSynOpTest {

    private static final String SLSTR_AUTO_GROUPING = "F*BT_*n:F*exception_*n:F*BT_*o:F*exception_*o:S*BT_in:S*exception_in:S*BT_io:S*exception_io:radiance_an:" +
            "S*exception_an:radiance_ao:S*exception_ao:radiance_bn:S*exception_bn:radiance_bo:S*exception_bo:radiance_cn:S*exception_cn:radiance_co:S*exception_co:" +
            "x_*:y_*:elevation:latitude:longitude:specific_humidity:temperature_profile:bayes_an_:bayes_ao_:bayes_bn_:bayes_bo_:bayes_cn_:bayes_co_:bayes_in_:bayes_io_:" +
            "cloud_an_:cloud_ao_:cloud_bn_:cloud_bo_:cloud_cn_:cloud_co_:cloud_in_:cloud_io_:confidence_an_:confidence_ao_:confidence_bn_:confidence_bo_:confidence_cn_:" +
            "confidence_co_:confidence_in_:confidence_io_:pointing_an_:pointing_ao_:pointing_bn_:pointing_bo_:pointing_cn_:pointing_co_:pointing_in_:pointing_io_:" +
            "S*_exception_an_*:S*_exception_ao_*:S*_exception_bn_*:S*_exception_bo_*:S*_exception_cn_*:S*_exception_co_*:S*_exception_in_*:S*_exception_io_*:" +
            "F*_exception_*n_*:F*_exception_*o_*:cartesian:cartesian:F1_quality:F2_quality:flags:geodetic:geometry:indices:" +
            "S1_quality:S2_quality:S3_quality:S4_quality:S5_quality:S6_quality:S7_quality:S8_quality:S9_quality:time";
    private static final String OLCI_AUTO_GROUPING = "Oa*_radiance:Oa*_radiance_err:atmospheric_temperature_profile:lambda0:FWHM:solar_flux";

    private static Product slstrProduct;
    private static Product olciProduct;

    @BeforeClass
    public static void initTestClass() throws IOException {
        String slstrFilePath = L1cSynOpTest.class.getResource("S3A_SL_1_RBT____20170313T110343_20170313T110643_20170314T172757_0179_015_208_2520_LN2_O_NT_002.SEN3.nc").getFile();
        slstrProduct = ProductIO.readProduct(slstrFilePath);
        slstrProduct.setAutoGrouping(SLSTR_AUTO_GROUPING);

        String olciFilePath = L1cSynOpTest.class.getResource("S3A_OL_1_EFR____20170313T110342_20170313T110642_20170314T162839_0179_015_208_2520_LN1_O_NT_002.nc").getFile();
        olciProduct = ProductIO.readProduct(olciFilePath);
        olciProduct.setAutoGrouping(OLCI_AUTO_GROUPING);
    }

    @Test
    public void testL1cSynOpTest() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        Product result = l1cSynOp.getTargetProduct();
        int numBands = result.getNumBands();
        String productType = result.getProductType();
        Band oa03band = result.getBand("Oa03_radiance");
        int height = result.getSceneRasterHeight();
        int width = result.getSceneRasterWidth();

        assertEquals(41, height);
        assertEquals(49, width);
        assertEquals(418, numBands);
        assertEquals("S3_L1C_SYN", productType);
        assertNotNull(oa03band);
    }

    @Test
    public void testGetCollocateParams() {
        L1cSynOp l1cSynOp = new L1cSynOp();
        Map<String, Object> map = l1cSynOp.getCollocateParams();
        boolean renameMasterComponents = (boolean) map.get("renameReferenceComponents");
        boolean renameSlaveComponents = (boolean) map.get("renameSecondaryComponents");
        String resamplingType = (String) map.get("resamplingType");
        String targetProductType = (String) map.get("targetProductType");
        assertFalse(renameMasterComponents);
        assertFalse(renameSlaveComponents);
        assertEquals("NEAREST_NEIGHBOUR", resamplingType);
        assertEquals("S3_L1C_SYN", targetProductType);
    }

    @Test
    public void testGetReprojectParams() {
        L1cSynOp l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        Map<String, Object> map = l1cSynOp.getReprojectParams();
        String resampling = (String) map.get("resampling");
        boolean orthorectify = (boolean) map.get("orthorectify");
        String crs = (String) map.get("crs");
        assertEquals("Nearest", resampling);
        assertFalse(orthorectify);
        assertEquals("EPSG:4326", crs);

    }

    @Test
    public void testGetResampleParameters() {
        L1cSynOp l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        Map<String, Object> map = l1cSynOp.getSlstrResampleParams(slstrProduct, "Nearest");
        int width = (int) map.get("targetWidth");
        int height = (int) map.get("targetHeight");
        String upsampling = (String) map.get("upsampling");
        String downsampling = (String) map.get("downsampling");
        String flagDownsampling = (String) map.get("flagDownsampling");
        boolean resampleOnPyramidLevels = (boolean) map.get("resampleOnPyramidLevels");
        assertEquals(30, width);
        assertEquals(24, height);
        assertEquals("Nearest", upsampling);
        assertEquals("First", downsampling);
        assertEquals("First", flagDownsampling);
        assertFalse(resampleOnPyramidLevels);

    }

    @Test
    public  void testBandSelection() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        String[] bandsOlci = {"Oa.._radiance", "FWHM_band_.*", "solar_flux_band_.*",
                "atmospheric_temperature_profile_.*", "TP_.*"};
        String[] bandsSlstr = {".*_an.*", ".*_ao.*"};
        l1cSynOp.setParameter("bandsOlci", bandsOlci);
        l1cSynOp.setParameter("bandsSlstr",bandsSlstr);
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        Product result = l1cSynOp.getTargetProduct();
        assertTrue(result.containsBand("S1_radiance_an"));
        assertTrue(result.containsBand("S3_radiance_ao"));
        assertTrue(result.containsBand("Oa03_radiance"));
        assertTrue(result.containsBand("FWHM_band_12"));
        assertTrue(result.containsBand("F1_exception_in"));
        assertTrue(result.containsBand("F2_exception_io"));
        assertTrue(result.containsBand("quality_flags"));
        assertFalse(result.containsBand("S4_radiance_bo"));
        assertFalse(result.containsBand("S5_radiance_cn"));
        assertFalse(result.containsBand("lambda0_band_5"));
    }

    @Test
    public void testDefaultNameCreation() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        Product result = l1cSynOp.getTargetProduct();
        assertTrue(result.getName().startsWith("S3A_SY_1_SYN____20170313T110342_20170313T110643"));
        assertTrue(result.getName().endsWith("0179_015_208_2520_LN2_O_NT____.SEN3"));
    }

    @Test
    public void testBandsRegExps() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setParameterDefaultValues();
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        l1cSynOp.setParameter("olciRegexp","Oa.._radiance, lambda0_band_.*");
        l1cSynOp.setParameter("slstrRegexp","S*._radiance_an, .*_an.*");
        Product result = l1cSynOp.getTargetProduct();
        assertTrue(result.containsBand("Oa01_radiance"));
        assertTrue(result.containsBand("Oa11_radiance"));
        assertTrue(result.containsBand("lambda0_band_3"));
        assertTrue(result.containsBand("bayes_an"));
        assertTrue(result.containsBand("S3_radiance_an"));
        assertTrue(result.containsBand("F1_exception_in"));
        assertTrue(result.containsBand("F2_exception_io"));
        assertTrue(result.containsBand("quality_flags"));
        assertFalse(result.containsBand("bayes_bn"));
        assertFalse(result.containsBand("solar_flux_band_4"));
        assertEquals(76,result.getNumBands());
    }

    @Test
    public void testWKTRegion() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        l1cSynOp.setParameterDefaultValues();
        l1cSynOp.setParameter("geoRegion","POLYGON((-16.936 26.715, -15.507  26.715 , -15.507 21.844 , -16.936     21.844 ,-16.936 26.715))");
        Product result = l1cSynOp.getTargetProduct();
        assertEquals(10,result.getSceneRasterWidth());
        assertEquals(21,result.getSceneRasterHeight());
    }

    @Test
    public void testNoReprojection() {
        Operator l1cSynOp = new L1cSynOp();
        l1cSynOp.setSourceProduct("olciProduct", olciProduct);
        l1cSynOp.setSourceProduct("slstrProduct", slstrProduct);
        l1cSynOp.setParameterDefaultValues();
        l1cSynOp.setParameter("stayOnOlciGrid",true);
        Product result = l1cSynOp.getTargetProduct();
        assertEquals("WGS84(DD)",result.getSceneGeoCoding().getMapCRS().getName().toString());
        assertEquals("Geodetic 2D",result.getSceneGeoCoding().getMapCRS().getCoordinateSystem().getName().toString());
        assertEquals(41,result.getSceneRasterHeight());
        assertEquals(49,result.getSceneRasterWidth());
    }
}