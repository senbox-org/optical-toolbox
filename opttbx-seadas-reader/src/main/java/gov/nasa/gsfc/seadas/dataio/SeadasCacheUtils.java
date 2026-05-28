package gov.nasa.gsfc.seadas.dataio;

import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.jspecify.annotations.NonNull;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.IOException;


public class SeadasCacheUtils {


    public static VariableDescriptor getDefaultVariableDescriptor(Variable netcdfVariable, String variableName) throws IOException {
        if (netcdfVariable == null) {
            throw new IOException("Variable not known: " + variableName);
        }

        final VariableDescriptor variableDescriptor = new VariableDescriptor();
        variableDescriptor.name = variableName;
        variableDescriptor.dataType = DataTypeUtils.getRasterDataType(netcdfVariable.getDataType(), false);

        final int[] shape = netcdfVariable.getShape();
        final Array chunkSizesValues;
        final Attribute chunkSizes = netcdfVariable.findAttribute("_ChunkSizes");
        if (chunkSizes != null) {
            chunkSizesValues = chunkSizes.getValues();
        } else {
            chunkSizesValues = Array.factory(DataType.INT, new int[]{shape.length}, shape);
        }

        if (shape.length == 2) {
            variableDescriptor.width = shape[1];
            variableDescriptor.height = shape[0];
            variableDescriptor.layers = -1;

            variableDescriptor.tileWidth = chunkSizesValues.getInt(1);
            variableDescriptor.tileHeight = chunkSizesValues.getInt(0);
            variableDescriptor.tileLayers = -1;
        } else if (shape.length == 3) {
            variableDescriptor.width = shape[2];
            variableDescriptor.height = shape[1];
            variableDescriptor.layers = shape[0];

            variableDescriptor.tileWidth = chunkSizesValues.getInt(2);
            variableDescriptor.tileHeight = chunkSizesValues.getInt(1);
            variableDescriptor.tileLayers = chunkSizesValues.getInt(0);
        } else {
            throw new IOException("Unsupported variable rank for caching: " + shape.length);
        }

        return variableDescriptor;
    }

    public static Array readArray(Variable netcdfVariable, int[] offsets, int[] shapes) throws IOException {
        try {
            return netcdfVariable.read(offsets, shapes);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    public static DataBuffer constructDataBuffer(Array rawBuffer, int[] offsets, int[] shapes, ProductData targetData, int rasterDataType) throws IOException {
        if (targetData == null) {
            targetData = createTargetDataBuffer(shapes, rasterDataType);
        }

        switch (rasterDataType) {
            case ProductData.TYPE_INT8:
            case ProductData.TYPE_UINT8:
                targetData.setElems(rawBuffer.get1DJavaArray(DataType.BYTE));
                break;
            case ProductData.TYPE_INT16:
            case ProductData.TYPE_UINT16:
                targetData.setElems(rawBuffer.get1DJavaArray(DataType.SHORT));
                break;
            case ProductData.TYPE_INT32:
            case ProductData.TYPE_UINT32:
                targetData.setElems(rawBuffer.get1DJavaArray(DataType.INT));
                break;
            case ProductData.TYPE_FLOAT32:
                targetData.setElems(rawBuffer.get1DJavaArray(DataType.FLOAT));
                break;
            case ProductData.TYPE_FLOAT64:
                targetData.setElems(rawBuffer.get1DJavaArray(DataType.DOUBLE));
                break;
            default:
                throw new IOException("Unknown data type: " + rasterDataType);
        }

        return new DataBuffer(targetData, offsets, shapes);
    }


    private static @NonNull ProductData createTargetDataBuffer(int[] shapes, int rasterDataType) throws IOException {
        ProductData targetData;
        if (shapes.length == 2) {
            targetData = ProductData.createInstance(rasterDataType, shapes[0] * shapes[1]);
        } else if (shapes.length == 3) {
            targetData = ProductData.createInstance(rasterDataType, shapes[0] * shapes[1] * shapes[2]);
        } else {
            throw new IOException("Illegally shaped variable");
        }
        return targetData;
    }
}
