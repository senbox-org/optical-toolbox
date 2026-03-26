package eu.esa.opt.spectralnoise;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.spectralnoise.util.SpectralNoiseParameter;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.OperatorException;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;


public class SpectralNoiseReductionOpTest {


    private static final double DOUBLE_ERR = 1.0e-10f;
    private static final float FLOAT_ERR = 1.0e-6f;


    @Test
    @STTM("SNAP-4173")
    public void test_InitializeCreatesTargetProductWithExpectedMetadata() throws Exception {
        Product sourceProduct = createSourceProduct();

        SpectralNoiseReductionOp op = createOperator(sourceProduct, null, 3);

        op.initialize();

        Product targetProduct = getField(op, "targetProduct", Product.class);

        assertNotNull(targetProduct);
        assertEquals("source_SNR", targetProduct.getName());
        assertEquals(sourceProduct.getProductType(), targetProduct.getProductType());
        assertEquals(sourceProduct.getSceneRasterWidth(), targetProduct.getSceneRasterWidth());
        assertEquals(sourceProduct.getSceneRasterHeight(), targetProduct.getSceneRasterHeight());
    }

    @Test
    @STTM("SNAP-4173")
    public void test_InitializeWithoutSourceBandsUsesAllSpectralBandsOnly() throws Exception {
        Product sourceProduct = createSourceProduct();

        SpectralNoiseReductionOp op = createOperator(sourceProduct, null, 3);

        op.initialize();

        Product targetProduct = getField(op, "targetProduct", Product.class);

        assertArrayEquals(new String[]{"band1", "band2", "band3", "band4"}, targetProduct.getBandNames());
    }

    @Test
    @STTM("SNAP-4173")
    public void test_InitializeWithExplicitSourceBandsUsesSelectedBandsOnly() throws Exception {
        Product sourceProduct = createSourceProduct();

        SpectralNoiseReductionOp op = createOperator(
                sourceProduct,
                new String[]{"band2", "band3", "band4"},
                3
        );

        op.initialize();

        Product targetProduct = getField(op, "targetProduct", Product.class);

        assertArrayEquals(new String[]{"band2", "band3", "band4"}, targetProduct.getBandNames());
    }

    @Test
    @STTM("SNAP-4173")
    public void test_InitializeCopiesBandMetadataToTargetBands() throws Exception {
        Product sourceProduct = createSourceProduct();

        SpectralNoiseReductionOp op = createOperator(
                sourceProduct,
                new String[]{"band1", "band2", "band3"},
                3
        );

        op.initialize();

        Product targetProduct = getField(op, "targetProduct", Product.class);
        Band sourceBand = sourceProduct.getBand("band1");
        Band targetBand = targetProduct.getBand("band1");

        assertNotNull(targetBand);
        assertEquals(sourceBand.getDataType(), targetBand.getDataType());
        assertEquals(sourceBand.getUnit(), targetBand.getUnit());
        assertEquals(sourceBand.getDescription(), targetBand.getDescription());
        assertEquals(sourceBand.getNoDataValue(), targetBand.getNoDataValue(), DOUBLE_ERR);
        assertEquals(sourceBand.isNoDataValueUsed(), targetBand.isNoDataValueUsed());
        assertEquals(sourceBand.getSpectralWavelength(), targetBand.getSpectralWavelength(), FLOAT_ERR);
        assertEquals(sourceBand.getSpectralBandwidth(), targetBand.getSpectralBandwidth(), FLOAT_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_InitializeThrowsWhenKernelSizeIsGreaterThanSelectedBandCount() throws Exception {
        Product sourceProduct = createSourceProduct();

        SpectralNoiseReductionOp op = createOperator(
                sourceProduct,
                new String[]{"band1", "band2"},
                3
        );

        try {
            op.initialize();
            fail("Expected OperatorException");
        } catch (OperatorException e) {
            assertEquals("Kernel size must not be greater than the number of selected bands.", e.getMessage());
        }
    }

    private SpectralNoiseReductionOp createOperator(Product sourceProduct,
                                                    String[] sourceBands,
                                                    int kernelSize) throws Exception {
        SpectralNoiseReductionOp op = new SpectralNoiseReductionOp();

        setField(op, "sourceProduct", sourceProduct);
        setField(op, "sourceBands", sourceBands);
        setField(op, "filterType", SpectralNoiseParameter.FILTER_BOX);
        setField(op, "kernelSize", kernelSize);
        setField(op, "gaussianSigma", 1.0);
        setField(op, "sgPolynomialOrder", 3);

        return op;
    }

    private Product createSourceProduct() {
        Product product = new Product("source", "hyperspectral-type", 7, 3);

        addSpectralBand(product, "band1", 500.0f, 10.0f, -9999.0, true, "sr", "Band 1 description");
        product.addBand("quality_flag", ProductData.TYPE_UINT8);
        addSpectralBand(product, "band2", 510.0f, 10.0f, -9998.0, false, "sr", "Band 2 description");
        addSpectralBand(product, "band3", 520.0f, 10.0f, -9997.0, true, "sr", "Band 3 description");
        addSpectralBand(product, "band4", 530.0f, 10.0f, -9996.0, true, "sr", "Band 4 description");

        return product;
    }

    private void addSpectralBand(Product product,
                                 String name,
                                 float wavelength,
                                 float bandwidth,
                                 double noDataValue,
                                 boolean noDataUsed,
                                 String unit,
                                 String description) {
        Band band = product.addBand(name, ProductData.TYPE_FLOAT32);
        band.setSpectralWavelength(wavelength);
        band.setSpectralBandwidth(bandwidth);
        band.setNoDataValue(noDataValue);
        band.setNoDataValueUsed(noDataUsed);
        band.setUnit(unit);
        band.setDescription(description);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private <T> T getField(Object target, String fieldName, Class<T> type) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}