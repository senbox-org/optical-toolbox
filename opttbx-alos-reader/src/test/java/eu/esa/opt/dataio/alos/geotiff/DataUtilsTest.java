package eu.esa.opt.dataio.alos.geotiff;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DataUtilsTest {

    @Test
    public void testGetDataType()  {
        assertEquals(ProductData.TYPE_UINT8, DataUtils.getDataType(8));
        assertEquals(ProductData.TYPE_INT16, DataUtils.getDataType(16));
        assertEquals(ProductData.TYPE_FLOAT32, DataUtils.getDataType(32));

        // trigger default case tb 2026-10-12
        assertEquals(ProductData.TYPE_UINT8, DataUtils.getDataType(11));
    }
}
