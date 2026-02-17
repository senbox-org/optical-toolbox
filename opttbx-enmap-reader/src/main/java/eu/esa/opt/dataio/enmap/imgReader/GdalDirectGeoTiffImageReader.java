package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.gdal.drivers.Band;
import org.esa.snap.dataio.gdal.drivers.Dataset;
import org.esa.snap.dataio.gdal.drivers.GDAL;
import org.esa.snap.dataio.gdal.drivers.GDALConst;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.getRelativePath;

class GdalDirectGeoTiffImageReader extends EnmapImageReader {

    private static final int MAX_CACHE_TILE_SIZE = 256;
    private static final int CE_NONE = 0;

    private final Dataset dataset;
    private final DatasetRasterReader datasetRasterReader;
    private final int numImages;
    private final Dimension tileDimension;
    private final int gdalDataType;
    private final int bytesPerSample;
    private final Object readLock;

    private GdalDirectGeoTiffImageReader(Dataset dataset, DatasetRasterReader datasetRasterReader, int numImages,
                                         Dimension tileDimension, int gdalDataType, int bytesPerSample) {
        this.dataset = dataset;
        this.datasetRasterReader = datasetRasterReader;
        this.numImages = numImages;
        this.tileDimension = tileDimension;
        this.gdalDataType = gdalDataType;
        this.bytesPerSample = bytesPerSample;
        this.readLock = new Object();
    }

    GdalDirectGeoTiffImageReader(DatasetRasterReader datasetRasterReader, int numImages,
                                 Dimension tileDimension, int gdalDataType, int bytesPerSample) {
        this(null, datasetRasterReader, numImages, tileDimension, gdalDataType, bytesPerSample);
    }

    static EnmapImageReader createImageReader(VirtualDir dataDir, String fileName, boolean isNonCompliantProduct) throws IOException {
        Dataset dataset = null;
        try {
            String relativePath = getRelativePath(dataDir, fileName, isNonCompliantProduct);
            dataset = GDAL.open(dataDir.getFile(relativePath).toString(), GDALConst.gaReadonly());
            if (dataset == null) {
                throw new IOException(String.format("Could not open GDAL dataset for '%s'.", fileName));
            }
            final int sceneWidth = dataset.getRasterXSize();
            final int sceneHeight = dataset.getRasterYSize();
            final int numImages = dataset.getRasterCount();
            if (numImages < 1) {
                throw new IOException(String.format("Dataset '%s' has no raster bands.", fileName));
            }
            final int gdalDataType;
            final Dimension tileDimension;
            try (Band firstBand = dataset.getRasterBand(1)) {
                if (firstBand == null) {
                    throw new IOException(String.format("Dataset '%s' has no first raster band.", fileName));
                }
                gdalDataType = firstBand.getDataType();
                tileDimension = new Dimension(
                        normalizeTileSize(firstBand.getBlockXSize(), sceneWidth),
                        normalizeTileSize(firstBand.getBlockYSize(), sceneHeight)
                );
            }
            final int bytesPerSample = Math.max(1, GDAL.getDataTypeSize(gdalDataType) >> 3);
            DatasetRasterReader rasterReader = ReflectionDatasetRasterReader.create(dataset.getJniDatasetInstance());
            return new GdalDirectGeoTiffImageReader(dataset, rasterReader, numImages, tileDimension, gdalDataType, bytesPerSample);
        } catch (Exception e) {
            if (dataset != null) {
                dataset.close();
            }
            throw new IOException("Could not create direct GDAL image reader.", e);
        }
    }

    @Override
    public Dimension getTileDimension() {
        return tileDimension;
    }

    @Override
    public int getNumImages() {
        return numImages;
    }

    @Override
    public RenderedImage getImageAt(int index) {
        throw new UnsupportedOperationException("Direct GDAL reader does not expose RenderedImage access.");
    }

    @Override
    public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) throws IOException {
        if (startLayer < 0 || numLayers <= 0 || (startLayer + numLayers) > numImages) {
            throw new IOException(String.format("Invalid layer request: startLayer=%d, numLayers=%d, availableLayers=%d.",
                    startLayer, numLayers, numImages));
        }
        final int elementCount = width * height * numLayers;
        if (targetData.getNumElems() < elementCount) {
            throw new IOException(String.format("Target buffer too small: required=%d, actual=%d.",
                    elementCount, targetData.getNumElems()));
        }
        final int pixelSpace = bytesPerSample;
        final int lineSpace = width * pixelSpace;
        final int bandSpace = width * height * pixelSpace;
        final int bufferSize = elementCount * bytesPerSample;
        final ByteBuffer nioBuffer = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        final int[] bandMap = buildContiguousOneBasedBandMap(startLayer, numLayers);

        final int returnCode;
        synchronized (readLock) {
            try {
                returnCode = datasetRasterReader.readRasterDirect(x, y, width, height, width, height, gdalDataType, nioBuffer,
                        bandMap, pixelSpace, lineSpace, bandSpace);
            } catch (Exception e) {
                throw new IOException(String.format("GDAL multiband read invocation failed for rect=[%d,%d,%d,%d], layers=%s.",
                        x, y, width, height, Arrays.toString(bandMap)), e);
            }
        }
        if (returnCode != CE_NONE) {
            throw new IOException(String.format("GDAL multiband read failed: returnCode=%d, rect=[%d,%d,%d,%d], layers=%s%s",
                    returnCode, x, y, width, height, Arrays.toString(bandMap), getLastErrorMessageSuffix()));
        }
        copyBufferToTarget(nioBuffer, targetData, elementCount);
    }

    @Override
    public boolean isInterleavedReadOptimized() {
        return true;
    }

    @Override
    public void close() {
        if (dataset != null) {
            try {
                dataset.close();
            } catch (IOException ignored) {
                // no-op
            }
        }
    }

    private static int[] buildContiguousOneBasedBandMap(int startLayer, int numLayers) {
        int[] bandMap = new int[numLayers];
        for (int i = 0; i < numLayers; i++) {
            bandMap[i] = startLayer + i + 1;
        }
        return bandMap;
    }

    private static void copyBufferToTarget(ByteBuffer sourceBuffer, ProductData targetData, int elementCount) throws IOException {
        sourceBuffer.rewind();
        final Object targetArray = targetData.getElems();
        if (targetArray instanceof byte[]) {
            sourceBuffer.get((byte[]) targetArray, 0, elementCount);
            return;
        }
        if (targetArray instanceof short[]) {
            sourceBuffer.asShortBuffer().get((short[]) targetArray, 0, elementCount);
            return;
        }
        if (targetArray instanceof int[]) {
            sourceBuffer.asIntBuffer().get((int[]) targetArray, 0, elementCount);
            return;
        }
        if (targetArray instanceof float[]) {
            sourceBuffer.asFloatBuffer().get((float[]) targetArray, 0, elementCount);
            return;
        }
        if (targetArray instanceof double[]) {
            sourceBuffer.asDoubleBuffer().get((double[]) targetArray, 0, elementCount);
            return;
        }
        fallbackCopy(sourceBuffer, targetData, elementCount);
    }

    private static void fallbackCopy(ByteBuffer sourceBuffer, ProductData targetData, int elementCount) throws IOException {
        final int targetType = targetData.getType();
        switch (targetType) {
            case ProductData.TYPE_INT8:
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemIntAt(i, sourceBuffer.get(i));
                }
                return;
            case ProductData.TYPE_UINT8:
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemIntAt(i, sourceBuffer.get(i) & 0xFF);
                }
                return;
            case ProductData.TYPE_INT16: {
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemIntAt(i, sourceBuffer.asShortBuffer().get(i));
                }
                return;
            }
            case ProductData.TYPE_UINT16: {
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemIntAt(i, sourceBuffer.asShortBuffer().get(i) & 0xFFFF);
                }
                return;
            }
            case ProductData.TYPE_INT32:
            case ProductData.TYPE_UINT32: {
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemIntAt(i, sourceBuffer.asIntBuffer().get(i));
                }
                return;
            }
            case ProductData.TYPE_FLOAT32: {
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemFloatAt(i, sourceBuffer.asFloatBuffer().get(i));
                }
                return;
            }
            case ProductData.TYPE_FLOAT64: {
                for (int i = 0; i < elementCount; i++) {
                    targetData.setElemDoubleAt(i, sourceBuffer.asDoubleBuffer().get(i));
                }
                return;
            }
            default:
                throw new IOException("Unsupported ProductData type for direct GDAL copy: " + targetType);
        }
    }

    private static String getLastErrorMessageSuffix() {
        try {
            String errorMsg = GDAL.getLastErrorMsg();
            if (errorMsg != null && !errorMsg.trim().isEmpty()) {
                return ", gdalError=\"" + errorMsg + "\"";
            }
        } catch (Throwable ignored) {
            // no-op
        }
        return "";
    }

    static int normalizeTileSize(int sourceTileSize, int sceneSize) {
        if (sceneSize <= 0) {
            return 1;
        }
        final int upperBound = Math.min(MAX_CACHE_TILE_SIZE, sceneSize);
        if (sourceTileSize <= 1 || sourceTileSize >= sceneSize) {
            return upperBound;
        }
        return Math.min(sourceTileSize, upperBound);
    }

    interface DatasetRasterReader {
        int readRasterDirect(int xOffset, int yOffset, int xSize, int ySize, int bufferXSize, int bufferYSize, int bufferType,
                             ByteBuffer nioBuffer, int[] bandMap, int pixelSpace, int lineSpace, int bandSpace) throws Exception;
    }

    private static final class ReflectionDatasetRasterReader implements DatasetRasterReader {
        private static final String METHOD_NAME = "ReadRaster_Direct";

        private final Object jniDatasetInstance;
        private final Method readRasterDirectMethod;

        private ReflectionDatasetRasterReader(Object jniDatasetInstance, Method readRasterDirectMethod) {
            this.jniDatasetInstance = jniDatasetInstance;
            this.readRasterDirectMethod = readRasterDirectMethod;
        }

        static ReflectionDatasetRasterReader create(Object jniDatasetInstance) throws NoSuchMethodException {
            Method method = resolveReadRasterDirectMethod(jniDatasetInstance.getClass());
            method.setAccessible(true);
            return new ReflectionDatasetRasterReader(jniDatasetInstance, method);
        }

        @Override
        public int readRasterDirect(int xOffset, int yOffset, int xSize, int ySize, int bufferXSize, int bufferYSize, int bufferType,
                                    ByteBuffer nioBuffer, int[] bandMap, int pixelSpace, int lineSpace, int bandSpace) throws Exception {
            Object result = readRasterDirectMethod.invoke(jniDatasetInstance, xOffset, yOffset, xSize, ySize,
                    bufferXSize, bufferYSize, bufferType, nioBuffer, bandMap, pixelSpace, lineSpace, bandSpace);
            return result instanceof Number ? ((Number) result).intValue() : CE_NONE;
        }

        private static Method resolveReadRasterDirectMethod(Class<?> datasetClass) throws NoSuchMethodException {
            try {
                return datasetClass.getMethod(METHOD_NAME,
                        int.class, int.class, int.class, int.class, int.class, int.class, int.class,
                        ByteBuffer.class, int[].class, int.class, int.class, int.class);
            } catch (NoSuchMethodException ignored) {
                for (Method method : datasetClass.getMethods()) {
                    if (METHOD_NAME.equals(method.getName()) && method.getParameterCount() == 12) {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        if (paramTypes[7].isAssignableFrom(ByteBuffer.class) && paramTypes[8].isAssignableFrom(int[].class)) {
                            return method;
                        }
                    }
                }
                throw new NoSuchMethodException("Cannot resolve Dataset.ReadRaster_Direct multiband signature.");
            }
        }
    }
}
