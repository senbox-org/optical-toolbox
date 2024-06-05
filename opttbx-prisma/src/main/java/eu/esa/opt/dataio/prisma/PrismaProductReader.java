package eu.esa.opt.dataio.prisma;

import com.bc.ceres.core.ProgressMonitor;
import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import io.jhdf.dataset.ContiguousDatasetImpl;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
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
import java.nio.ShortBuffer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.DATASET_NAME_LONGITUDE;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.GROUP_NAME_PRS_L2D_HCO;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaProductReader extends AbstractProductReader {

    private Map<Band, Integer> _cubeIndex = new HashMap<>();
    private StringBuilder _autoGrouping = new StringBuilder();

    private String _prefixErrorMessage;
    private HdfFile _hdfFile;
    private int _sceneRasterWidth;
    private int _sceneRasterHeight;

    private boolean _vnirCubeExist;
    private ContiguousDatasetImpl _vnirCube;
    private boolean _swirCubeExist;
    private ContiguousDatasetImpl _swirCube;

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
        final Group prs_l2d_hco = (Group) findNode(_hdfFile, GROUP_NAME_PRS_L2D_HCO);
        if (prs_l2d_hco == null) {
            throw new ProductIOException(_prefixErrorMessage + "Group '" + GROUP_NAME_PRS_L2D_HCO + "' not found.");
        }
        final Dataset longitude = (Dataset) findNode(prs_l2d_hco, DATASET_NAME_LONGITUDE);
        if (longitude == null) {
            throw new ProductIOException(_prefixErrorMessage + "Could not find dataset \"" + DATASET_NAME_LONGITUDE + "\" for file " + inputPath);
        }
        final int[] dimensions = longitude.getDimensions();
        _sceneRasterWidth = dimensions[1];
        _sceneRasterHeight = dimensions[0];
        final Product product = new Product(fileName, "ASI Prisma", _sceneRasterWidth, _sceneRasterHeight);
        MetadataReader.readMetadata(_hdfFile, product);

        addCubeBands(prs_l2d_hco, product);

        addGeoCoding(product);

        product.setAutoGrouping(_autoGrouping.toString());

        return product;
    }

    private void addGeoCoding(Product product) {
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
            final Dataset latitude = (Dataset) findNode(_hdfFile, "Latitude");
            final Dataset longitude = (Dataset) findNode(_hdfFile, "Longitude");
            if (latitude != null && longitude != null) {
                final float[] latitudes = (float[]) latitude.getData();
                final float[] longitudes = (float[]) longitude.getData();
//                product.setGeoCoding(PrismaGeoCoding.createGeoCoding(latitudes, longitudes, _sceneRasterWidth, _sceneRasterHeight));
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

    private void addCubeBands(Group prs_l2d_hco, Product product) throws ProductIOException {
        final Node vnirCube = findNode(prs_l2d_hco, "VNIR_Cube");
        _vnirCubeExist = vnirCube instanceof Dataset;
        if (_vnirCubeExist) {
            _vnirCube = (ContiguousDatasetImpl) vnirCube;
            final String cubeName = "Vnir";
            addCubeBands(cubeName, product, _vnirCube);
            appendAutoGrouping(cubeName);
        }

        final Node swirCube = findNode(prs_l2d_hco, "SWIR_Cube");
        _swirCubeExist = swirCube instanceof Dataset;
        if (_swirCubeExist) {
            _swirCube = (ContiguousDatasetImpl) swirCube;
            final String cubeName = "Swir";
            addCubeBands(cubeName, product, _swirCube);
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
        final float scaleMin = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Min").getData();
        final float scaleMax = (float) getGlobalAttributeNotNull("L2Scale" + cubeName + "Max").getData();
        final double scalingFactor = (scaleMax - scaleMin) / 65535;

        final float[] wavelengths = (float[]) getGlobalAttributeNotNull("List_Cw_" + cubeName).getData();
        final float[] bandwidths = (float[]) getGlobalAttributeNotNull("List_Fwhm_" + cubeName).getData();

        final int numBands = getNumBands(cube);
        for (int i = 0; i < numBands; i++) {
            final float wavelength = wavelengths[i];
            if (wavelength == 0.0f) {
                continue;
            }
            String bandName = String.format(cubeName + "_Rrs_%02d", i + 1);
            final Band band = new Band(bandName, ProductData.TYPE_UINT16, _sceneRasterWidth, _sceneRasterHeight) {
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

        long[] sliceOffset = {sourceOffsetY, _cubeIndex.get(destBand), sourceOffsetX};
        int[] sliceDimensions = {destHeight, 1, destWidth};

        final ByteBuffer sliceByteBuffer;
        if (destBand.getName().startsWith("Vnir")) {
            sliceByteBuffer = _vnirCube.getSliceDataBuffer(sliceOffset, sliceDimensions);
        } else {
            sliceByteBuffer = _swirCube.getSliceDataBuffer(sliceOffset, sliceDimensions);
        }
        final ShortBuffer shortBuffer = sliceByteBuffer.asShortBuffer();
        final short[] shorts = (short[]) destBuffer.getElems();
        shortBuffer.get(shorts);
    }

    private static Node findNode(Group group, String name) {
        for (Node node : group) {
            final String nodeName = node.getName();
            if (nodeName.equalsIgnoreCase(name)) {
                return node;
            }
            if (node instanceof Group) {
                final Node found = findNode((Group) node, name);
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
    }
}
