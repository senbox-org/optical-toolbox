package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.annotation.STTM;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


public class S3CachedGeoDataUtilTest {


    @Test
    @STTM("SNAP-4200")
    public void test_readCachedRawDataAsDouble() throws Exception {
        ProductCache productCache = mock(ProductCache.class);

        doAnswer(invocation -> {
            DataBuffer buffer = invocation.getArgument(3);
            ProductData data = buffer.getData();

            data.setElemFloatAt(0, 1.0f);
            data.setElemFloatAt(1, 2.5f);
            data.setElemFloatAt(2, -3.0f);
            data.setElemFloatAt(3, 4.25f);
            data.setElemFloatAt(4, 5.5f);
            data.setElemFloatAt(5, 6.75f);

            return null;
        }).when(productCache).read(eq("longitude"), any(int[].class), any(int[].class), any(DataBuffer.class));

        double[] result = S3CachedGeoDataUtil.readCachedRawDataAsDouble(
                productCache, "longitude", 3, 2, ProductData.TYPE_FLOAT32
        );

        assertArrayEquals(new double[]{1.0, 2.5, -3.0, 4.25, 5.5, 6.75}, result, 1.0e-8);
        verify(productCache).read(eq("longitude"), aryEq(new int[]{0, 0}), aryEq(new int[]{2, 3}), any(DataBuffer.class));
    }

    @Test
    @STTM("SNAP-4200")
    public void test_readCachedGeophysicalBandAsDouble() throws Exception {
        ProductCache productCache = mock(ProductCache.class);
        Band band = new Band("latitude", ProductData.TYPE_FLOAT32, 2, 2);
        band.setScalingFactor(2.0);
        band.setScalingOffset(10.0);

        doAnswer(invocation -> {
            DataBuffer buffer = invocation.getArgument(3);
            ProductData data = buffer.getData();

            data.setElemFloatAt(0, 1.0f);
            data.setElemFloatAt(1, 2.0f);
            data.setElemFloatAt(2, 3.0f);
            data.setElemFloatAt(3, 4.0f);

            return null;
        }).when(productCache).read(eq("latitude"), any(int[].class), any(int[].class), any(DataBuffer.class));

        double[] result = S3CachedGeoDataUtil.readCachedGeophysicalBandAsDouble(productCache, band);

        assertArrayEquals(new double[]{12.0, 14.0, 16.0, 18.0}, result, 1.0e-8);
        verify(productCache).read(eq("latitude"), aryEq(new int[]{0, 0}), aryEq(new int[]{2, 2}), any(DataBuffer.class));
    }
}