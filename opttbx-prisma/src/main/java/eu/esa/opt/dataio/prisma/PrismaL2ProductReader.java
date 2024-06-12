package eu.esa.opt.dataio.prisma;

import com.bc.ceres.core.ProgressMonitor;
import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.dataset.ContiguousDatasetImpl;
import io.jhdf.object.datatype.DataType;
import io.jhdf.object.datatype.FixedPoint;
import io.jhdf.object.datatype.FloatingPoint;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoCodingFactory;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_CUBE_MEASUREMENT_NAME;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_CUBE_UNIT;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_L2_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_L2_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaL2ProductReader extends AbstractProductReader {

    private final Map<Band, Integer> _cubeIndex = new HashMap<>();
    private final Map<Band, Dataset> _datasetMapping = new HashMap<>();
    private final StringBuilder _autoGrouping = new StringBuilder();
    private final Map<ComparableIntArray, GeoCoding> _geoCodingLookUp = new HashMap<>();
    private final TreeSet<ComparableIntArray> _dimsSet = new TreeSet<>();

    private String _prefixErrorMessage;
    private HdfFile _hdfFile;
    private String _productLevel;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected PrismaL2ProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        Path inputPath = convertToPath(getInput());
        assert inputPath != null;
        _prefixErrorMessage = "Unable to open '" + inputPath.toAbsolutePath() + ". ";
        final String fileName = inputPath.getFileName().toString();
        if (!PRISMA_L2_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new ProductIOException(_prefixErrorMessage + "The file name: '" + fileName + "' does not match the regular expression: " + PRISMA_L2_FILENAME_REGEX);
        }
        final String lowerFileName = fileName.toLowerCase();
        final String justTheName = fileName.substring(0, fileName.lastIndexOf("."));
        if (lowerFileName.endsWith(".zip")) {
            URI uri = URI.create("jar:file:" + inputPath.toUri().getPath());
            final FileSystem zipFileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
            final Path zipInternalRoot = zipFileSystem.getRootDirectories().iterator().next();
            final List<Path> list = Files.list(zipInternalRoot).collect(Collectors.toList());
            Path hdf5Path = null;
            for (Path path : list) {
                if (path.getFileName().toString().equals(justTheName + ".he5")) {
                    hdf5Path = path;
                }
            }
            if (hdf5Path != null) {
                _hdfFile = new HdfFile(hdf5Path);
            }
        } else if (lowerFileName.endsWith(".he5")) {
            _hdfFile = new HdfFile(inputPath);
        }
        if (_hdfFile == null) {
            throw new ProductIOException(_prefixErrorMessage + "HDF could not be opened.");
        }
        _productLevel = (String) getGlobalAttributeNotNull("Processing_Level").getData();

        collectDimensionsAndDatasets(_hdfFile, _dimsSet, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        int sceneRasterWidth = _dimsSet.first().array[1];
        int sceneRasterHeight = _dimsSet.first().array[0];
        final Product product = new Product(fileName, "ASI Prisma", sceneRasterWidth, sceneRasterHeight);
        product.setProductReader(this);
        MetadataReader.readMetadata(_hdfFile, product);

        final Group swathsGroup = (Group) _hdfFile.getByPath("/HDFEOS/SWATHS");
        for (Map.Entry<String, Node> entry : swathsGroup.getChildren().entrySet()) {
            String key = entry.getKey();
            if (!key.startsWith("PRS_")) {
                continue;
            }
            final String dataGroupName = key.substring(key.lastIndexOf("_") + 1);
            appendAutoGrouping(dataGroupName);
            Node node = entry.getValue();
            if (!(node instanceof Group)) {
                continue;
            }
            final Group group = (Group) node;
            final TreeSet<ComparableIntArray> dimensionsSet = new TreeSet<>();
            final HashMap<String, Dataset> datasetMap1D = new HashMap<>();
            final HashMap<String, Dataset> tinyDatasetMap2D = new HashMap<>();
            final HashMap<String, Dataset> datasetMap2D = new HashMap<>();
            final HashMap<String, Dataset> datasetMap3D = new HashMap<>();
            collectDimensionsAndDatasets(group, dimensionsSet, datasetMap1D, tinyDatasetMap2D, datasetMap2D, datasetMap3D);
            GeoCoding geoCoding = createGeoCoding(dataGroupName, datasetMap2D, product);
            for (Dataset dataset : datasetMap2D.values()) {
                addBand(dataGroupName, product, geoCoding, dataset);
            }
            for (Dataset dataset : datasetMap3D.values()) {
                addCubeBands(dataGroupName, product, geoCoding, (ContiguousDatasetImpl) dataset);
            }
        }

        product.setAutoGrouping(_autoGrouping.toString());

        return product;
    }

    private Band addBand(String dataGroupName, Product product, GeoCoding geoCoding, Dataset dataset) throws ProductIOException {
        final String datasetName = dataset.getName();
        final String bandName = dataGroupName + "_" + datasetName;
        final Band existingBand = product.getBand(bandName);
        if (existingBand != null) {
            return existingBand;
        }
        final int[] dimensions = dataset.getDimensions();
        final int width = dimensions[1];
        final int height = dimensions[0];
        final DataType dataType = dataset.getDataType();
        final int productDataType = getProductDataType(dataType);
        final Band band = new Band(bandName, productDataType, width, height) {
            @Override
            public boolean isProductReaderDirectlyUsable() {
                return true;
            }
        };
        if (geoCoding != null) {
            band.setGeoCoding(geoCoding);
        }
        product.addBand(band);
        _datasetMapping.put(band, dataset);
        return band;
    }

    private GeoCoding createGeoCoding(String dataGroupName, HashMap<String, Dataset> datasetMap2D, Product product) throws IOException {
        final Attribute epsgCodeAtt = getGlobalAttribute("Epsg_Code");
        final Dataset latDS;
        final Dataset lonDS;
        if (datasetMap2D.containsKey("Latitude") && datasetMap2D.containsKey("Longitude")) {
            latDS = datasetMap2D.get("Latitude");
            lonDS = datasetMap2D.get("Longitude");
        } else if (datasetMap2D.containsKey("Latitude_SWIR") && datasetMap2D.containsKey("Longitude_SWIR")) {
            latDS = datasetMap2D.get("Latitude_SWIR");
            lonDS = datasetMap2D.get("Longitude_SWIR");
        } else if (datasetMap2D.containsKey("Latitude_VNIR") && datasetMap2D.containsKey("Longitude_VNIR")) {
            latDS = datasetMap2D.get("Latitude_VNIR");
            lonDS = datasetMap2D.get("Longitude_VNIR");
        } else {
            return null;
        }
        if (epsgCodeAtt != null) {
            final int[] dimensions = latDS.getDimensions();
            final ComparableIntArray lookUpKey = new ComparableIntArray(dimensions);
            if (_geoCodingLookUp.containsKey(lookUpKey)) {
                return _geoCodingLookUp.get(lookUpKey);
            }
            final int width = dimensions[1];
            final int height = dimensions[0];

            final long epsgCodeValue = (long) epsgCodeAtt.getData();

            String[] nameParts = {"Product_", "easting"};
            float minEasting = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxEasting = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            nameParts = new String[]{"Product_", "northing"};
            float minNorthing = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxNorthing = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            double scaleX = (maxEasting - minEasting) / (width - 1);
            double scaleY = (maxNorthing - minNorthing) / (height - 1);

            AffineTransform i2m = new AffineTransform();
            i2m.translate(minEasting, maxNorthing);
            i2m.scale(scaleX, -scaleY);

            String epsgCode = "EPSG:" + epsgCodeValue;
            try {
                CoordinateReferenceSystem mapCRS = CRS.decode(epsgCode);
                final CrsGeoCoding geoCoding = new CrsGeoCoding(mapCRS, width, height, minEasting, maxNorthing, scaleX, scaleY);
                _geoCodingLookUp.put(lookUpKey, geoCoding);
                return geoCoding;
            } catch (FactoryException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not decode EPSG code: " + epsgCodeValue, e);
            } catch (TransformException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not create CrsGeoCoding.", e);
            }
        }
        final Band latBand = addBand(dataGroupName, product, null, latDS);
        final Band lonBand = addBand(dataGroupName, product, null, lonDS);
        final ComponentGeoCoding pixelGeoCoding = GeoCodingFactory.createPixelGeoCoding(latBand, lonBand);
        latBand.setGeoCoding(pixelGeoCoding);
        lonBand.setGeoCoding(pixelGeoCoding);
        return pixelGeoCoding;
    }

    private float getComputedGlobalAttValue(String[] nameParts, float minEasting, BinaryOperator<Float> mathOperator) {
        for (Attribute attribute : _hdfFile.getAttributes().values()) {
            final String name = attribute.getName();
            if (Arrays.stream(nameParts).allMatch(name::contains)) {
                minEasting = mathOperator.apply(minEasting, (float) attribute.getData());
            }
        }
        return minEasting;
    }

    private void appendAutoGrouping(String s) {
        if (_autoGrouping.length() > 0) {
            _autoGrouping.append(":");
        }
        _autoGrouping.append(s);
    }

    private void addCubeBands(String dataGroupName, Product product, GeoCoding geoCoding, ContiguousDatasetImpl cube) throws ProductIOException {
        final String datasetName = cube.getName();
        final boolean measurementCube = datasetName.toLowerCase().contains("cube");
        if (measurementCube) {
            final String measurementName = LEVEL_DEPENDENT_CUBE_MEASUREMENT_NAME.get(_productLevel);
            final String cubeUnit = LEVEL_DEPENDENT_CUBE_UNIT.get(_productLevel);
            final String cubeName = datasetName.toLowerCase().contains("vnir") ? "Vnir" : "Swir";
            final float scaleMin = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Min").getData();
            final float scaleMax = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Max").getData();
            final float[] wavelengths = (float[]) getGlobalAttributeNotNull("List_Cw_" + cubeName).getData();
            final float[] bandwidths = (float[]) getGlobalAttributeNotNull("List_Fwhm_" + cubeName).getData();
            final int[] dimensions = cube.getDimensions();
            final int width = dimensions[2];
            final int height = dimensions[0];
            final int numBands = dimensions[1];
            final int numDigits = ("" + numBands).length();
            final String autogrouping = dataGroupName + "_" + cubeName;
            appendAutoGrouping(autogrouping);
            final String bandNameFormatExpression = autogrouping + measurementName + "_%0" + numDigits + "d";
            final DataType dataType = cube.getDataType();
            final int productDataType = getProductDataType(dataType);
            final double scalingFactor;
            if (ProductData.TYPE_UINT16 == productDataType) {
                scalingFactor = (scaleMax - scaleMin) / 65535;
            } else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported cube data type.");
            }
            for (int i = 0; i < numBands; i++) {
                final float wavelength = wavelengths[i];
                if (wavelength == 0.0f) {
                    continue;
                }
                String bandName = String.format(bandNameFormatExpression, i + 1);
                final Band band = new Band(bandName, productDataType, width, height) {
                    @Override
                    public boolean isProductReaderDirectlyUsable() {
                        return true;
                    }
                };
                _cubeIndex.put(band, i);
                band.setSpectralWavelength(wavelength);
                band.setSpectralBandwidth(bandwidths[i]);
                band.setScalingFactor(scalingFactor);
                band.setScalingOffset(scaleMin);
                band.setUnit(cubeUnit);
                if (geoCoding != null) {
                    band.setGeoCoding(geoCoding);
                }
                product.addBand(band);
                _datasetMapping.put(band, cube);
            }
        } else {
            final int[] dimensions = cube.getDimensions();
            final int width = dimensions[2];
            final int height = dimensions[0];
            final int numBands = dimensions[1];
            final int numDigits = ("" + numBands).length();
            final String autogrouping = dataGroupName + "_" + datasetName;
            appendAutoGrouping(autogrouping);
            final String bandNameFormatExpression = autogrouping + "_%0" + numDigits + "d";
            final DataType dataType = cube.getDataType();
            final int productDataType = getProductDataType(dataType);
            for (int i = 0; i < numBands; i++) {
                String bandName = String.format(bandNameFormatExpression, i + 1);
                final Band band = new Band(bandName, productDataType, width, height) {
                    @Override
                    public boolean isProductReaderDirectlyUsable() {
                        return true;
                    }
                };
                _cubeIndex.put(band, i);
//                band.setSpectralWavelength(wavelength);
//                band.setSpectralBandwidth(bandwidths[i]);
//                band.setScalingFactor(scalingFactor);
//                band.setScalingOffset(scaleMin);
//                band.setUnit(cubeUnit);
                if (geoCoding != null) {
                    band.setGeoCoding(geoCoding);
                }
                product.addBand(band);
                _datasetMapping.put(band, cube);
            }
        }
    }

    private int getProductDataType(DataType dataType) throws ProductIOException {
        int productDataType;
        if (dataType instanceof FloatingPoint) {
            final FloatingPoint floatingPoint = (FloatingPoint) dataType;
            final short bitPrecision = floatingPoint.getBitPrecision();
            if (bitPrecision == 32) {
                productDataType = ProductData.TYPE_FLOAT32;
            } else if (bitPrecision == 64) {
                productDataType = ProductData.TYPE_FLOAT64;
            } else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
            }
        } else if (dataType instanceof FixedPoint) {
            FixedPoint fixedPoint = (FixedPoint) dataType;
            final boolean signed = fixedPoint.isSigned();
            final short bitPrecision = fixedPoint.getBitPrecision();
            if (bitPrecision == 8) {
                productDataType = signed ? ProductData.TYPE_INT8 : ProductData.TYPE_UINT8;
            } else if (bitPrecision == 16) {
                productDataType = signed ? ProductData.TYPE_INT16 : ProductData.TYPE_UINT16;
            } else if (bitPrecision == 32) {
                productDataType = signed ? ProductData.TYPE_INT32 : ProductData.TYPE_UINT32;
            } else if (bitPrecision == 64) {
                productDataType = signed ? ProductData.TYPE_INT64 : ProductData.TYPE_UINT64;
            } else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
            }
        } else {
            throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
        }
        return productDataType;
    }

    private Attribute getGlobalAttributeNotNull(String attributeName) throws ProductIOException {
        final Attribute attribute = getGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new ProductIOException(_prefixErrorMessage + "Global Attribute '" + attributeName + "' not found.");
        }
        return attribute;
    }

    private Attribute getGlobalAttribute(String attributeName) {
        return _hdfFile.getAttribute(attributeName);
    }

    @Override
    protected void readBandRasterDataImpl(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand,
            int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) {

        if (_cubeIndex.containsKey(destBand)) {
            readFromCube(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                         destBand, destWidth, destHeight, destBuffer, pm);
        } else {
            readFromDataset(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                            destBand, destWidth, destHeight, destBuffer, pm);
        }
        pm.worked(sourceWidth * sourceHeight);
    }

    private void readFromDataset(
            int srcOffsetX, int srcOffsetY, int srcWidth, int srcHeight, int srcStepX, int srcStepY,
            Band destBand, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) {
        final Dataset dataset = _datasetMapping.get(destBand);

        long[] sliceOffset = {srcOffsetY, srcOffsetX};
        int[] sliceDimensions = {srcHeight, srcWidth};
        if (srcOffsetX == 0 && srcOffsetY == 0 && srcWidth == destBand.getRasterWidth() && srcHeight == destBand.getRasterHeight()) {
            final Object src = dataset.getDataFlat();
            destBuffer.setElems(src);
        } else {
            final ByteBuffer sliceByteBuffer =
                    ((ContiguousDatasetImpl) dataset).getSliceDataBuffer(sliceOffset, sliceDimensions);
            PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                    sliceByteBuffer, srcWidth, srcHeight, srcStepX, srcStepY,
                    destBuffer, destWidth, destHeight);
        }
    }

    private void readFromCube(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) {
        final ContiguousDatasetImpl cube = (ContiguousDatasetImpl) _datasetMapping.get(destBand);
        long[] sliceOffset = {sourceOffsetY, _cubeIndex.get(destBand), sourceOffsetX};
        int[] sliceDimensions = {sourceHeight, 1, sourceWidth};
        final ByteBuffer sliceByteBuffer = cube.getSliceDataBuffer(sliceOffset, sliceDimensions);
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceByteBuffer, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                destBuffer, destWidth, destHeight);
    }

    @Override
    public void close() throws IOException {
        super.close();
        _hdfFile.close();
        _cubeIndex.clear();
        _datasetMapping.clear();
        _dimsSet.clear();
    }

    private static void collectDimensionsAndDatasets(Group group, TreeSet<ComparableIntArray> dimensionsSet,
                                                     Map<String, Dataset> datasetMap1D,
                                                     Map<String, Dataset> tinyDatasetMap2D,
                                                     Map<String, Dataset> datasetMap2D,
                                                     Map<String, Dataset> datasetMap3D) {
        group.getChildren().forEach((s, node) -> {
            if (node.isGroup()) {
                final Group gn = (Group) node;
                collectDimensionsAndDatasets(gn, dimensionsSet, datasetMap1D, tinyDatasetMap2D, datasetMap2D, datasetMap3D);
            } else if (!node.isLink() && !node.isAttributeCreationOrderTracked()) {
                final Dataset dataset = (Dataset) node;
                final int[] dimensions = dataset.getDimensions();
                if (dimensions.length == 1 || dimensions.length == 2 && Arrays.stream(dimensions).anyMatch(v -> v == 1)) {
                    datasetMap1D.put(dataset.getName(), dataset);
                } else if (dimensions.length >= 2 && dimensions[0] > 256 && dimensions[dimensions.length - 1] > 256) {
                    dimensionsSet.add(new ComparableIntArray(dimensions));
                    if (dimensions.length == 2) {
                        datasetMap2D.put(dataset.getName(), dataset);
                    } else {
                        datasetMap3D.put(dataset.getName(), dataset);
                    }
                } else {
                    tinyDatasetMap2D.put(dataset.getName(), dataset);
                }
            }
        });
    }

    private static class ComparableIntArray implements Comparable<ComparableIntArray> {
        private final int[] array;

        public ComparableIntArray(int[] array) {
            this.array = array;
        }

        public int[] getArray() {
            return array;
        }

        @Override
        public int compareTo(ComparableIntArray o) {
            final int[] other = o.getArray();
            if (array.length != other.length) {
                return Integer.compare(array.length, other.length);
            }
            for (int i = 0; i < array.length; i++) {
                if (array[i] != other[i]) {
                    return Integer.compare(other[i], array[i]);
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return Arrays.toString(array);
        }
    }

}
