package eu.esa.opt.dataio.s3.util;

import org.esa.snap.dataio.netcdf.util.ArrayConverter;
import org.esa.snap.dataio.netcdf.util.DimKey;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.util.List;

public final class S3CacheLayerMapper {

    private S3CacheLayerMapper() {
    }

    public static boolean isLayered(Variable variable) {
        return variable.getRank() > 2 && DimKey.findStartIndexOfBandVariables(variable.getDimensions()) >= 0;
    }

    public static String createCacheKey(String variableName, ArrayConverter converter) {
        if (converter == ArrayConverter.LSB) {
            return variableName + "_lsb";
        }
        if (converter == ArrayConverter.MSB) {
            return variableName + "_msb";
        }
        return variableName;
    }

    public static int getLayer(Variable variable, int[] imageOrigin) {
        return flattenLayerIndex(imageOrigin, createLayerShape(variable));
    }

    public static int[] createLayerShape(Variable variable) {
        final List<Dimension> dims = variable.getDimensions();
        final int startIndexToCopy = DimKey.findStartIndexOfBandVariables(dims);
        if (variable.getRank() <= 2 || startIndexToCopy < 0) {
            return new int[0];
        }

        final int[] layerShape = new int[variable.getRank() - 2];
        for (int i = 0; i < layerShape.length; i++) {
            layerShape[i] = dims.get(startIndexToCopy + i).getLength();
        }
        return layerShape;
    }

    public static int flattenLayerIndex(int[] origin, int[] layerShape) {
        if (origin.length == 0 || layerShape.length == 0) {
            return 0;
        }

        int layer = 0;
        final int length = Math.min(origin.length, layerShape.length);
        for (int i = 0; i < length; i++) {
            layer *= layerShape[i];
            layer += origin[i];
        }
        return layer;
    }

    public static final class LayerReference {
        private final String cacheKey;
        private final int layer;

        public LayerReference(String cacheKey, int layer) {
            this.cacheKey = cacheKey;
            this.layer = layer;
        }

        public String getCacheKey() {
            return cacheKey;
        }

        public int getLayer() {
            return layer;
        }
    }
}
