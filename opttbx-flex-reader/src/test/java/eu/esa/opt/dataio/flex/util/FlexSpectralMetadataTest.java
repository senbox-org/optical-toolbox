package eu.esa.opt.dataio.flex.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.flex.FlexProductReader;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

public class FlexSpectralMetadataTest {

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_oneDimensionalValues() {
        final Product product = new Product("p", "t", 1, 1);
        addSpectralMetadata(product, "wavelength", new float[]{550.0f, 680.0f, 740.0f}, null);

        final float[] values = FlexSpectralMetadata.resolveReference(product, "wavelength", 3);

        assertArrayEquals(new float[]{550.0f, 680.0f, 740.0f}, values, 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_twoDimensionalValuesUseMedianIgnoringNaNs() {
        final Product product = new Product("p", "t", 1, 1);
        addSpectralMetadata(product, "wavelength", new float[]{Float.NaN, Float.NaN, Float.NaN, 10.0f, 20.0f, 30.0f, 14.0f, Float.NaN, 32.0f, 12.0f, 22.0f, 34.0f}, null);

        final float[] values = FlexSpectralMetadata.resolveReference(product, "wavelength", 3);

        assertArrayEquals(new float[]{12.0f, 21.0f, 32.0f}, values, 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_ignoresFillValues() {
        final Product product = new Product("p", "t", 1, 1);
        addSpectralMetadata(product, "wavelength", new float[]{-1.0f, 20.0f, 10.0f, 22.0f, 12.0f, -1.0f, 14.0f, 24.0f}, -1.0f);

        final float[] values = FlexSpectralMetadata.resolveReference(product, "wavelength", 2);

        assertArrayEquals(new float[]{12.0f, 22.0f}, values, 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_invalidInputsReturnNoValues() {
        final Product product = new Product("p", "t", 1, 1);
        addSpectralMetadata(product, "wavelength", new float[]{550.0f}, null);

        assertEquals(0, FlexSpectralMetadata.resolveReference(product, null, 1).length);
        assertEquals(0, FlexSpectralMetadata.resolveReference(product, "", 1).length);
        assertEquals(0, FlexSpectralMetadata.resolveReference(product, "wavelength", 0).length);
        assertEquals(0, FlexSpectralMetadata.resolveReference(product, "missing", 1).length);
        assertEquals(0, FlexSpectralMetadata.resolveReference(product, "wavelength", 2).length);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_missingNetcdfMetadataReturnsNoValues() {
        final Product product = new Product("p", "t", 1, 1);

        final float[] values = FlexSpectralMetadata.resolveReference(product, "wavelength", 1);

        assertEquals(0, values.length);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReference_missingValueAttributeReturnsNoValues() {
        final Product product = new Product("p", "t", 1, 1);
        final MetadataElement netcdf = getOrCreateNetcdfElement(product);
        netcdf.addElement(new MetadataElement("wavelength"));

        final float[] values = FlexSpectralMetadata.resolveReference(product, "wavelength", 1);

        assertEquals(0, values.length);
    }

    @Test
    @STTM("SNAP-4126")
    public void testResolveReferences_collectsWavelengthAndFwhmReferences() {
        final Product product = new Product("p", "t", 1, 1);
        addSpectralMetadata(product, "wavelength", new float[]{500.0f, 510.0f, 520.0f}, null);
        addSpectralMetadata(product, "fwhm", new float[]{1.0f, 2.0f}, null);
        final FlexVariableDescriptor firstDescriptor = specialDescriptor("wavelength", "fwhm", 2);
        final FlexVariableDescriptor secondDescriptor = specialDescriptor("wavelength", null, 3);

        final Map<String, float[]> references = FlexSpectralMetadata.resolveReferences(product, Arrays.asList(firstDescriptor, secondDescriptor));

        assertArrayEquals(new float[]{500.0f, 510.0f, 520.0f}, references.get("wavelength"), 1.0e-6f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, references.get("fwhm"), 1.0e-6f);
        assertEquals(2, references.size());
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralWavelength_usesOneBasedChannelIndex() {
        final Band band = new Band("b", ProductData.TYPE_FLOAT32, 1, 1);

        FlexSpectralMetadata.setSpectralWavelength(band, new float[]{500.0f, 510.0f}, 2);
        assertEquals(510.0f, band.getSpectralWavelength(), 1.0e-6f);
    }

    @Test
    @STTM("SNAP-4126")
    public void testSetSpectralBandwidth_ignoresInvalidValues() {
        final Band nanBand = new Band("nan", ProductData.TYPE_FLOAT32, 1, 1);
        final Band zeroBand = new Band("zero", ProductData.TYPE_FLOAT32, 1, 1);
        final Band missingBand = new Band("missing", ProductData.TYPE_FLOAT32, 1, 1);

        FlexSpectralMetadata.setSpectralBandwidth(nanBand, new float[]{Float.NaN}, 1);
        FlexSpectralMetadata.setSpectralBandwidth(zeroBand, new float[]{0.0f}, 1);
        FlexSpectralMetadata.setSpectralBandwidth(missingBand, new float[]{1.0f}, 2);

        assertEquals(0.0f, nanBand.getSpectralBandwidth(), 1.0e-6f);
        assertEquals(0.0f, zeroBand.getSpectralBandwidth(), 1.0e-6f);
        assertEquals(0.0f, missingBand.getSpectralBandwidth(), 1.0e-6f);
    }

    private static FlexVariableDescriptor specialDescriptor(String wavelengthReference, String fwhmReference, int depth) {
        final FlexVariableDescriptor descriptor = new FlexVariableDescriptor();
        descriptor.setType('s');
        descriptor.setWavelengthReference(wavelengthReference);
        descriptor.setFwhmReference(fwhmReference);
        descriptor.setDepth(depth);
        return descriptor;
    }

    private static void addSpectralMetadata(Product product, String name, float[] values, Float fillValue) {
        final MetadataElement netcdf = getOrCreateNetcdfElement(product);
        final MetadataElement element = new MetadataElement(name);
        element.addAttribute(new MetadataAttribute("value", ProductData.createInstance(values), true));
        if (fillValue != null) {
            element.addAttribute(new MetadataAttribute("_FillValue",
                    ProductData.createInstance(new float[]{fillValue}), true));
        }
        netcdf.addElement(element);
    }

    private static MetadataElement getOrCreateNetcdfElement(Product product) {
        MetadataElement netcdf = product.getMetadataRoot().getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (netcdf == null) {
            netcdf = new MetadataElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
            product.getMetadataRoot().addElement(netcdf);
        }
        return netcdf;
    }
}
