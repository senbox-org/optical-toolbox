package eu.esa.opt.dataio.prisma;

import com.bc.ceres.core.ProgressMonitor;
import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.DATASET_NAME_LONGITUDE;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.GROUP_NAME_PRS_L2D_HCO;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaProductReader extends AbstractProductReader {
    private HdfFile _hdfFile;
    private int _sceneRasterWidth;
    private int _sceneRasterHeight;
    private boolean _swirCubeExist;
    private Dataset _swirCube;
    private Dataset _vnirCube;
    private String _prefix_message;
    private boolean _vnirCubeExist;

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
        _prefix_message = "Unable to open '" + inputPath.toAbsolutePath() + ". ";
        final String fileName = inputPath.getFileName().toString();
        if (!PRISMA_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new ProductIOException(_prefix_message + "The file name: '" + fileName + "' does not match the regular expression: " + PRISMA_FILENAME_REGEX);
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
            throw new ProductIOException(_prefix_message + "HDF could not be opened.");
        }
        final Group prs_l2d_hco = (Group) findNode(_hdfFile, GROUP_NAME_PRS_L2D_HCO);
        if (prs_l2d_hco == null) {
            throw new ProductIOException(_prefix_message + "Group '" + GROUP_NAME_PRS_L2D_HCO + "' not found.");
        }
        final Dataset longitude = (Dataset) findNode(prs_l2d_hco, DATASET_NAME_LONGITUDE);
        if (longitude == null) {
            throw new ProductIOException(_prefix_message + "Could not find dataset \"" + DATASET_NAME_LONGITUDE + "\" for file " + inputPath);
        }
        final int[] dimensions = longitude.getDimensions();
        _sceneRasterWidth = dimensions[0];
        _sceneRasterHeight = dimensions[1];
        final Product product = new Product(fileName, "ASI Prisma", _sceneRasterWidth, _sceneRasterHeight);
        MetadataReader.readMetadata(_hdfFile, product);

        final Node vnirCube = findNode(prs_l2d_hco, "VNIR_Cube");
        _vnirCubeExist = vnirCube instanceof Dataset;
        if (_vnirCubeExist) {
            _vnirCube = (Dataset) vnirCube;
            final Attribute l2ScaleVnirMin = _hdfFile.getAttribute("L2ScaleVnirMin");
            if (l2ScaleVnirMin == null) {
                throw new ProductIOException(_prefix_message + "Global Attribute 'L2ScaleVnirMin' not found.");
            }

            final int numBands = getNumBands(_vnirCube);
            for (int i = 0; i < numBands; i++) {
                final String bandName = String.format("Vnir_Rrs_%02d", i + 1);
                final Band band = product.addBand(bandName, ProductData.TYPE_INT16);
                band.setSpectralWavelength();
                band.setSpectralBandIndex();
                band.setScalingFactor();
                band.setScalingOffset();
            }
        }

        final Node swirCube = findNode(prs_l2d_hco, "SWIR_Cube");
        _swirCubeExist = swirCube instanceof Dataset;
        if (_swirCubeExist) {
            _swirCube = (Dataset) swirCube;
        }

        return product;
    }

    private int getNumBands(Dataset cube) {
        final int[] dim = cube.getDimensions();
        int numBands= 0;
        for (int i : dim) {
            if (i != _sceneRasterHeight && i != _sceneRasterWidth) {
                numBands = i;
                break;
            }
        }
        return numBands;
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
                                          Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

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
