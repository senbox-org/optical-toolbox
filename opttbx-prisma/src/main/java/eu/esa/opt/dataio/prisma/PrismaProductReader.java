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
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.GeoCodingFactory;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
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
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.DATASET_NAME_LONGITUDE;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.GROUP_NAME_PATTERN_HCO;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_CUBE_KONTEXT_IDENTIFIER;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaProductReader extends AbstractProductReader {

    private Map<Band, Integer> _cubeIndex = new HashMap<>();
    private Map<Band, Dataset> _datasetMapping = new HashMap<>();
    private StringBuilder _autoGrouping = new StringBuilder();

    private String _prefixErrorMessage;
    private HdfFile _hdfFile;
    private int _sceneRasterWidth;
    private int _sceneRasterHeight;
    private TreeSet<String> dimsSet = new TreeSet<>();
    private TreeMap<String, Dataset> datasetMap2D = new TreeMap<>();
    private TreeMap<String, Dataset> datasetMap3D = new TreeMap<>();

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected PrismaProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        Path inputPath = convertToPath(getInput());
        assert inputPath != null;
        _prefixErrorMessage = "Unable to open '" + inputPath.toAbsolutePath() + ". ";
        final String fileName = inputPath.getFileName().toString();
        if (!PRISMA_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new ProductIOException(_prefixErrorMessage + "The file name: '" + fileName + "' does not match the regular expression: " + PRISMA_FILENAME_REGEX);
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
        collectDimensionsAndDatasets(_hdfFile);
        final Group prs_hco_grp = (Group) findNode(_hdfFile, GROUP_NAME_PATTERN_HCO);
        if (prs_hco_grp == null) {
            throw new ProductIOException(_prefixErrorMessage + "Group '" + GROUP_NAME_PATTERN_HCO + "' not found.");
        }
        final Dataset longitude = (Dataset) findNode(prs_hco_grp, DATASET_NAME_LONGITUDE);
        if (longitude == null) {
            throw new ProductIOException(_prefixErrorMessage + "Could not find dataset \"" + DATASET_NAME_LONGITUDE + "\" for file " + inputPath);
        }
        final int[] dimensions = longitude.getDimensions();
        _sceneRasterWidth = dimensions[1];
        _sceneRasterHeight = dimensions[0];
        final Product product = new Product(fileName, "ASI Prisma", _sceneRasterWidth, _sceneRasterHeight);
        MetadataReader.readMetadata(_hdfFile, product);

        addCubeBands(prs_hco_grp, product);
        addBands(prs_hco_grp, product);

        addGeoCoding(product);

        product.setAutoGrouping(_autoGrouping.toString());

        return product;
    }

    private void addBands(Group group, Product product) {
        final Dataset latitude = (Dataset) findNode(group, "Latitude");
        if (latitude != null) {
            final Band latitudeBand = new Band("Latitude", ProductData.TYPE_FLOAT64, _sceneRasterWidth, _sceneRasterHeight);
            latitudeBand.setUnit("degree");
            product.addBand(latitudeBand);
            _datasetMapping.put(latitudeBand, latitude);
        }
        final Dataset longitude = (Dataset) findNode(group, "Longitude");
        if (longitude != null) {
            final Band longitudeBand = new Band("Longitude", ProductData.TYPE_FLOAT64, _sceneRasterWidth, _sceneRasterHeight);
            longitudeBand.setUnit("degree");
            product.addBand(longitudeBand);
            _datasetMapping.put(longitudeBand, longitude);
        }
    }

    private void addGeoCoding(Product product) throws IOException {
        final Attribute epsgCodeAtt = getGlobalAttribute("Epsg_Code");
        if (epsgCodeAtt != null) {
            final long epsgCodeValue = (long) epsgCodeAtt.getData();

            String[] nameParts = {"Product_", "easting"};
            float minEasting = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxEasting = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            nameParts = new String[]{"Product_", "northing"};
            float minNorthing = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxNorthing = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            double scaleX = (maxEasting - minEasting) / (_sceneRasterWidth - 1);
            double scaleY = (maxNorthing - minNorthing) / (_sceneRasterHeight - 1);

            AffineTransform i2m = new AffineTransform();
            i2m.translate(minEasting, maxNorthing);
            i2m.scale(scaleX, -scaleY);

            String epsgCode = "EPSG:" + epsgCodeValue;
            try {
                CoordinateReferenceSystem mapCRS = CRS.decode(epsgCode);
                final CrsGeoCoding geoCoding = new CrsGeoCoding(mapCRS, _sceneRasterWidth, _sceneRasterHeight, minEasting, maxNorthing, scaleX, scaleY);
                product.setSceneGeoCoding(geoCoding);
            } catch (FactoryException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not decode EPSG code: " + epsgCodeValue, e);
            } catch (TransformException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not create CrsGeoCoding.", e);
            }
        } else {
            final Band latBand = product.getBand("Latitude");
            final Band lonBand = product.getBand("Longitude");
            if (latBand != null && lonBand != null) {
                product.setSceneGeoCoding(GeoCodingFactory.createPixelGeoCoding(latBand, lonBand));
            }
        }
    }

    private float getComputedGlobalAttValue(String[] nameParts, float minEasting, BinaryOperator<Float> mathOperator) {
        for (Attribute attribute : _hdfFile.getAttributes().values()) {
            final String name = attribute.getName();
            if (Arrays.stream(nameParts).allMatch(namePart -> name.contains(namePart))) {
                minEasting = mathOperator.apply(minEasting, (float) attribute.getData());
            }
        }
        return minEasting;
    }

    private void addCubeBands(Group group, Product product) throws ProductIOException {
        final Node vnirCube = findNode(group, "VNIR_Cube");
        boolean vnirCubeExist = vnirCube instanceof Dataset;
        if (vnirCubeExist) {
            ContiguousDatasetImpl cube = (ContiguousDatasetImpl) vnirCube;
            final String cubeName = "Vnir";
            addCubeBands(cubeName, product, cube);
            appendAutoGrouping(cubeName);
        }

        final Node swirCube = findNode(group, "SWIR_Cube");
        boolean swirCubeExist = swirCube instanceof Dataset;
        if (swirCubeExist) {
            ContiguousDatasetImpl cube = (ContiguousDatasetImpl) swirCube;
            final String cubeName = "Swir";
            addCubeBands(cubeName, product, cube);
            appendAutoGrouping(cubeName);
        }
    }

    private void appendAutoGrouping(String s) {
        if (_autoGrouping.length() > 0) {
            _autoGrouping.append(":");
        }
        _autoGrouping.append(s);
    }

    private void addCubeBands(String cubeName, Product product, ContiguousDatasetImpl cube) throws ProductIOException {
        final String processingLevel = (String) getGlobalAttributeNotNull("Processing_Level").getData();
        final String kontextIdentifier = LEVEL_DEPENDENT_CUBE_KONTEXT_IDENTIFIER.get(processingLevel);
        final float scaleMin = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Min").getData();
        final float scaleMax = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Max").getData();
        final double scalingFactor = (scaleMax - scaleMin) / 65535;

        final float[] wavelengths = (float[]) getGlobalAttributeNotNull("List_Cw_" + cubeName).getData();
        final float[] bandwidths = (float[]) getGlobalAttributeNotNull("List_Fwhm_" + cubeName).getData();

        final int[] dimensions = cube.getDimensions();
        final int width = dimensions[2];
        final int height = dimensions[0];
        final int numBands = getNumBands(cube);
        final int numDigits = ("" + numBands).length();
        int productDataType = -1;
        final DataType dataType = cube.getDataType();
        if (dataType instanceof FixedPoint) {
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
            }   else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
            }
        }
        for (int i = 0; i < numBands; i++) {
            final float wavelength = wavelengths[i];
            if (wavelength == 0.0f) {
                continue;
            }
            String bandName = String.format(cubeName + kontextIdentifier + "_%0"+ numDigits +"d", i + 1);
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
            band.setUnit("1");
            product.addBand(band);
            _datasetMapping.put(band, cube);
        }
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

    private int getNumBands(Dataset cube) {
        final int[] dim = cube.getDimensions();
        int numBands = 0;
        for (int i : dim) {
            if (i != _sceneRasterHeight && i != _sceneRasterWidth) {
                numBands = i;
                break;
            }
        }
        return numBands;
    }

    @Override
    protected void readBandRasterDataImpl(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand,
            int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

        if (destBand.getName().startsWith("Vnir") || destBand.getName().startsWith("Swir")) {
            readFromCube(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                         destBand, destOffsetX, destOffsetY, destWidth, destHeight, destBuffer, pm);
        } else {
            readFromDataset(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                            destBand, destOffsetX, destOffsetY, destWidth, destHeight, destBuffer, pm);
        }
        pm.worked(sourceWidth * sourceHeight);
    }

    private void readFromDataset(
            int srcOffsetX, int srcOffsetY, int srcWidth, int srcHeight, int srcStepX, int srcStepY,
            Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) {
        final ContiguousDatasetImpl dataset = (ContiguousDatasetImpl) _datasetMapping.get(destBand);

        long[] sliceOffset = {srcOffsetY, srcOffsetX};
        int[] sliceDimensions = {srcHeight, srcWidth};
        final ByteBuffer sliceByteBuffer = dataset.getSliceDataBuffer(sliceOffset, sliceDimensions);
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceByteBuffer, srcWidth, srcHeight, srcStepX, srcStepY,
                destBuffer, destOffsetX, destOffsetY, destWidth, destHeight);
    }

    private void readFromCube(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) {
        final ContiguousDatasetImpl cube = (ContiguousDatasetImpl) _datasetMapping.get(destBand);
        long[] sliceOffset = {sourceOffsetY, _cubeIndex.get(destBand), sourceOffsetX};
        int[] sliceDimensions = {sourceHeight, 1, sourceWidth};
        final ByteBuffer sliceByteBuffer = cube.getSliceDataBuffer(sliceOffset, sliceDimensions);
        PrismaConstantsAndUtils.datatypeDependentDataTransfer(
                sliceByteBuffer, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                destBuffer, destOffsetX, destOffsetY, destWidth, destHeight);
    }

    private static Node findNode(Group group, String name) {
        final Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);

        return findNode(group, pattern);
    }

    private static Node findNode(Group group, Pattern pattern) {
        for (Node node : group) {
            final String nodeName = node.getName();
            if (pattern.matcher(nodeName).matches()) {
                return node;
            }
            if (node instanceof Group) {
                final Node found = findNode((Group) node, pattern);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        super.close();
        _hdfFile.close();
        _cubeIndex.clear();
        _datasetMapping.clear();
    }

    private void collectDimensionsAndDatasets(Group group) {
        group.getChildren().forEach((s, node) -> {
            if (node.isGroup()) {
                final Group gn = (Group) node;
//                writer.println("    " + gn.getPath());
                collectDimensionsAndDatasets(gn);
            } else if (!node.isLink() && !node.isAttributeCreationOrderTracked()) {
                final Dataset dataset = (Dataset) node;
                final int[] dimensions = dataset.getDimensions();
                if (dimensions.length > 1 && dimensions[0] > 256 && dimensions[dimensions.length - 1] > 256) {
                    final String dimString = Arrays.toString(dimensions);
                    dimsSet.add(dimString);
                    if (dimensions.length == 2) {
                        datasetMap2D.put(dataset.getPath(), dataset);
                    } else {
                        datasetMap3D.put(dataset.getPath(), dataset);
                    }

//                    if (dimensions.length >1) {
//                        final String parentPath = dataset.getParent().getPath();
//                        if (!parentPath.equals(lastParentPath)) {
//                            lastParentPath = parentPath;
//                            printWriter.println("    " + parentPath);
//                        }
//                        printWriter.print("        " + node.getName());
//                        printWriter.println(" --> " + dimString);
//                    }
                }
            }
        });
    }
}
