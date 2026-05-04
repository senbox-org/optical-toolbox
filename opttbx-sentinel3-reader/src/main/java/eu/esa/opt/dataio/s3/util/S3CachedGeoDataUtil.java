package eu.esa.opt.dataio.s3.util;

import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.ProductData;

import java.io.IOException;


public final class S3CachedGeoDataUtil {


    private S3CachedGeoDataUtil() {}


    public static double[] readCachedRawDataAsDouble(ProductCache productCache, String cacheKey, int width, int height, int sourceDataType) throws IOException {
        final int[] offsets = new int[]{0, 0};
        final int[] shapes = new int[]{height, width};

        final DataBuffer buffer = new DataBuffer(sourceDataType, offsets, shapes);
        productCache.read(cacheKey, offsets, shapes, buffer);

        final ProductData sourceData = buffer.getData();
        final double[] target = new double[width * height];

        for (int ii = 0; ii < target.length; ii++) {
            target[ii] = sourceData.getElemDoubleAt(ii);
        }

        return target;
    }

    public static double[] readCachedGeophysicalBandAsDouble(ProductCache productCache, Band band) throws IOException {
        final String cacheKey = band.getName();
        final int width = band.getRasterWidth();
        final int height = band.getRasterHeight();
        final int sourceDataType = band.getDataType();

        final double[] data = readCachedRawDataAsDouble(productCache, cacheKey, width, height, sourceDataType);

        for (int ii = 0; ii < data.length; ii++) {
            data[ii] = S3Util.getGeophysicalValue(band, data[ii]);
        }

        return data;
    }
}
