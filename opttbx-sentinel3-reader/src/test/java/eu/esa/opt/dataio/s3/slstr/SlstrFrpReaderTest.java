package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class SlstrFrpReaderTest {

    @Test
    @STTM("SNAP-1691")
    public void testAddBandAndDataProvider() {
        final Product product = new Product("bla", "blub");
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement variableAttributes = new MetadataElement("Variable_Attributes");
        variableAttributes.addElement(new MetadataElement("Newband"));
        metadataRoot.addElement(variableAttributes);

        SlstrFRPReader.BandInfo bandInfo = new SlstrFRPReader.BandInfo("Newband", ProductData.TYPE_FLOAT32);

        SlstrFRPReader.addBandAndDataProvider(product, bandInfo, 250, 600);
        final Band newband = product.getBand("Newband");
        assertNotNull(newband);
        assertEquals(ProductData.TYPE_FLOAT32, newband.getDataType());

        assertEquals(250, newband.getRasterWidth());
        assertEquals(600, newband.getRasterHeight());

        assertTrue(newband.isNoDataValueSet());
        assertEquals(9.96921E36F, newband.getNoDataValue(), 1e-8);
    }

    @Test
    @STTM("SNAP-1691")
    public void testGetDefaultFillValue() {
        assertEquals(9.969209968386869E36, SlstrFRPReader.getDefaultFillValue(ProductData.TYPE_FLOAT64).doubleValue(), 1e-8);
        assertEquals(9.96921E36f, SlstrFRPReader.getDefaultFillValue(ProductData.TYPE_FLOAT32).floatValue(), 1e-8);
        assertEquals(-2147483647, SlstrFRPReader.getDefaultFillValue(ProductData.TYPE_INT32).intValue());
    }

    @Test
    @STTM("SNAP-1691")
    public void testGetDefaultFillValue_unsupportedDataType() {
        try {
            SlstrFRPReader.getDefaultFillValue(ProductData.TYPE_INT8);
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {
        }
    }
}
