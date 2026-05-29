package eu.esa.opt.dataio.flex;

import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Dimension;
import ucar.nc2.Variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class FlexDirectNetcdfBandReader {

    private static final String[] TYPICAL_X_DIM_NAMES = {
            "lon", "long", "longitude", "ni", "NX", "SX", "x", "xc",
            "across_track", "number_of_across_track_samples",
            "numCells", "col", "cols", "column", "columns", "tie_columns"
    };

    private static final String[] TYPICAL_Y_DIM_NAMES = {
            "lat", "latitude", "nj", "NY", "SY", "y", "yc",
            "along_track", "number_of_along_track_samples",
            "numRows", "row", "rows", "tie_rows"
    };

    private final Object readLock = new Object();

    void read(Variable variable,
              int dataType,
              int sourceOffsetX, int sourceOffsetY,
              int sourceWidth, int sourceHeight,
              int sourceStepX, int sourceStepY,
              int destWidth, int destHeight,
              ProductData destBuffer) throws IOException {

        read(variable, -1, dataType,
                sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight,
                sourceStepX, sourceStepY, destWidth, destHeight, destBuffer);
    }

    void readLayer(Variable variable,
                   int layer,
                   int dataType,
                   int sourceOffsetX, int sourceOffsetY,
                   int sourceWidth, int sourceHeight,
                   int sourceStepX, int sourceStepY,
                   int destWidth, int destHeight,
                   ProductData destBuffer) throws IOException {

        read(variable, layer, dataType,
                sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight,
                sourceStepX, sourceStepY, destWidth, destHeight, destBuffer);
    }

    private void read(Variable variable,
                      int layer,
                      int dataType,
                      int sourceOffsetX, int sourceOffsetY,
                      int sourceWidth, int sourceHeight,
                      int sourceStepX, int sourceStepY,
                      int destWidth, int destHeight,
                      ProductData destBuffer) throws IOException {
        if (variable == null) {
            throw new IOException("NetCDF variable is missing.");
        }
        if (sourceStepX < 1 || sourceStepY < 1) {
            throw new IOException("Invalid subsampling step for variable: " + variable.getFullName());
        }

        final int rank = variable.getRank();
        if (rank < 2) {
            throw new IOException("Variable is not raster-like: " + variable.getFullName());
        }

        final List<Dimension> dimensions = variable.getDimensions();
        final int xIndex = findDimensionIndex(dimensions, TYPICAL_X_DIM_NAMES, rank - 1);
        final int yIndex = findDimensionIndex(dimensions, TYPICAL_Y_DIM_NAMES, rank - 2);

        final int[] origin = new int[rank];
        final int[] shape = new int[rank];
        final int[] stride = new int[rank];
        Arrays.fill(shape, 1);
        Arrays.fill(stride, 1);

        origin[xIndex] = sourceOffsetX;
        origin[yIndex] = sourceOffsetY;
        shape[xIndex] = sourceWidth;
        shape[yIndex] = sourceHeight;
        stride[xIndex] = sourceStepX;
        stride[yIndex] = sourceStepY;

        setLayerOrigin(layer, dimensions, xIndex, yIndex, origin);

        Array array;
        synchronized (readLock) {
            try {
                array = variable.read(new Section(origin, shape, stride));
            } catch (InvalidRangeException e) {
                throw new IOException("Invalid range reading variable: " + variable.getFullName(), e);
            }
        }

        if (xIndex < yIndex) {
            array = array.transpose(xIndex, yIndex);
        }

        final long expectedSize = (long) destWidth * destHeight;
        if (array.getSize() != expectedSize) {
            throw new IOException("Unexpected data size reading variable " + variable.getFullName()
                    + ": expected " + expectedSize + " but got " + array.getSize());
        }
        destBuffer.setElems(array.get1DJavaArray(toNetcdfDataType(dataType)));
    }

    private static void setLayerOrigin(int layer, List<Dimension> dimensions,
                                       int xIndex, int yIndex, int[] origin) throws IOException {
        final int[] layerIndices = createLayerIndices(dimensions.size(), xIndex, yIndex);
        if (layerIndices.length == 0) {
            if (layer > 0) {
                throw new IOException("Layer index out of range: " + layer);
            }
            return;
        }

        final int[] layerShape = createLayerShape(dimensions, layerIndices);
        final int[] layerOrigin = unflattenLayerIndex(Math.max(layer, 0), layerShape);
        for (int i = 0; i < layerIndices.length; i++) {
            origin[layerIndices[i]] = layerOrigin[i];
        }
    }

    private static int findDimensionIndex(List<Dimension> dimensions, String[] typicalNames, int fallbackIndex) {
        for (int i = 0; i < dimensions.size(); i++) {
            final String dimName = dimensions.get(i).getShortName();
            if (dimName != null) {
                for (final String typicalName : typicalNames) {
                    if (dimName.equalsIgnoreCase(typicalName)) {
                        return i;
                    }
                }
            }
        }
        return fallbackIndex;
    }

    private static int[] createLayerIndices(int rank, int xIndex, int yIndex) {
        final int[] layerIndices = new int[rank - 2];
        int targetIndex = 0;
        for (int i = 0; i < rank; i++) {
            if (i != xIndex && i != yIndex) {
                layerIndices[targetIndex++] = i;
            }
        }
        return layerIndices;
    }

    private static int[] createLayerShape(List<Dimension> dimensions, int[] layerIndices) {
        final int[] layerShape = new int[layerIndices.length];
        for (int i = 0; i < layerIndices.length; i++) {
            layerShape[i] = dimensions.get(layerIndices[i]).getLength();
        }
        return layerShape;
    }

    private static int getNumLayers(int[] layerShape) {
        int layers = 1;
        for (final int size : layerShape) {
            layers *= size;
        }
        return layers;
    }

    private static int[] unflattenLayerIndex(int layer, int[] layerShape) throws IOException {
        final int layerCount = getNumLayers(layerShape);
        if (layer < 0 || layer >= layerCount) {
            throw new IOException("Layer index out of range: " + layer + " (layers: " + layerCount + ")");
        }

        final int[] origin = new int[layerShape.length];
        for (int i = layerShape.length - 1; i >= 0; i--) {
            origin[i] = layer % layerShape[i];
            layer /= layerShape[i];
        }
        return origin;
    }

    private static DataType toNetcdfDataType(int productDataType) throws IOException {
        return switch (productDataType) {
            case ProductData.TYPE_INT8 -> DataType.BYTE;
            case ProductData.TYPE_UINT8 -> DataType.UBYTE;
            case ProductData.TYPE_INT16 -> DataType.SHORT;
            case ProductData.TYPE_UINT16 -> DataType.USHORT;
            case ProductData.TYPE_INT32 -> DataType.INT;
            case ProductData.TYPE_UINT32 -> DataType.UINT;
            case ProductData.TYPE_INT64 -> DataType.LONG;
            case ProductData.TYPE_FLOAT32 -> DataType.FLOAT;
            case ProductData.TYPE_FLOAT64 -> DataType.DOUBLE;
            default -> throw new IOException("Unsupported ProductData type for direct NetCDF read: " + productDataType);
        };
    }
}
