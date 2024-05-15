package eu.esa.opt.dataio.prisma;

import com.bc.ceres.core.ProgressMonitor;
import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Dataset;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToPath;

public class PrismaProductReader extends AbstractProductReader {
    private Path inputPath;
    private HdfFile _hdfFile;

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
        inputPath = convertToPath(getInput());
        final String fileName = inputPath.getFileName().toString();
        if (!PRISMA_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new ProductIOException("The file name: '" + fileName + "' does not match the regular expression: " + PRISMA_FILENAME_REGEX);
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
            throw new ProductIOException("Unable to open '" + inputPath.toAbsolutePath());
        }
        final Group prs_l2d_hco = (Group) findNode(_hdfFile, "PRS_L2D_HCO");
        final Dataset longitude = (Dataset) findNode(prs_l2d_hco, "Longitude");
        if (longitude == null) {
            throw new ProductIOException("Could not find dataset \"longitude\" for file " + inputPath);
        }
        final int[] dimensions = longitude.getDimensions();
        final Product product = new Product(fileName, "ASI Prisma", dimensions[0], dimensions[1]);
        readMetadata(product);

        final Node swirCube = findNode(prs_l2d_hco, "SWIR_Cube");
        if (swirCube != null) {
            final Dataset cube = (Dataset) swirCube;
        }

        return product;

//        if (lowerName.matches("prs_l.*\\.zip") || lowerName.matches("prs_l.*\\.he5")) {
//            rootPath = inputPath;
//        } else {
//            rootPath = inputPath.getParent();
//        }
//        assert rootPath != null;
//        if (Files.isRegularFile(rootPath)) {
//            store = new ZipStore(rootPath);
//        } else {
//            store = new FileSystemStore(rootPath);
//        }
//        rootGroup = ZarrGroup.open(store);
    }

    private void readMetadata(Product product) throws IllegalFileFormatException {
        final MetadataElement root = product.getMetadataRoot();
        final MetadataElement globalAttributes = new MetadataElement("GlobalAttributes");
        root.addElement(globalAttributes);
        final Map<String, Attribute> attributes = _hdfFile.getAttributes();
        addAttributesTo(globalAttributes, attributes);
        final Map<String, Node> children = _hdfFile.getChildren();
        addAttributeTreeTo(root, children);


    }

    private void addAttributeTreeTo(MetadataElement element, Map<String, Node> children) throws IllegalFileFormatException {
        for (Node child : children.values()) {
            final String childName = child.getName();
            final MetadataElement nestedElement = new MetadataElement(childName);
            final Map<String, Attribute> attributes = child.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                addAttributesTo(nestedElement, attributes);
            }
            if (child instanceof Group) {
                addAttributeTreeTo(nestedElement, ((Group) child).getChildren());
            }
            if (nestedElement.getNumAttributes() > 0 || nestedElement.getNumElements() > 0) {
                element.addElement(nestedElement);
            }
        }
    }

    private static void addAttributesTo(MetadataElement element, Map<String, Attribute> attributes) throws IllegalFileFormatException {
        for (Attribute att : attributes.values()) {
            final String attName = att.getName();
            final boolean scalar = att.isScalar();
            final Class<?> javaType = att.getJavaType();
            final Object data = att.getData();
            ProductData pd = null;
            if (scalar) {
                if (data instanceof Short) {
                    pd = ProductData.createInstance(ProductData.TYPE_INT16, 1);
                    pd.setElemInt((Short) data);
                } else if (data instanceof Integer) {
                    pd = ProductData.createInstance(ProductData.TYPE_INT32, 1);
                    pd.setElemInt((Integer) data);
                } else if (data instanceof Long) {
                    pd = ProductData.createInstance(ProductData.TYPE_INT64, 1);
                    pd.setElemLong((Long) data);
                } else if (data instanceof Float) {
                    pd = ProductData.createInstance(ProductData.TYPE_FLOAT32, 1);
                    pd.setElemFloat((Float) data);
                } else if (data instanceof String) {
                    pd = ProductData.createInstance((String) data);
                } else {
                    throw new IllegalFileFormatException("Unsupported data type: " + data.getClass().getCanonicalName());
                }
                if (pd != null) {
                    final MetadataAttribute ma = new MetadataAttribute(attName, pd, true);
                    element.addAttribute(ma);
                }
            } else {
                final int[] dimensions = att.getDimensions();
                final MetadataElement arrayAttElem = new MetadataElement(attName);
                if (dimensions.length == 1) {
                    if (data instanceof Integer) {
                        pd = ProductData.createInstance(ProductData.TYPE_INT32, att.getDimensions()[0]);
                        pd.setElems(data);
                    } else if (javaType == int.class) {
                        pd = ProductData.createInstance((int[]) data);
                    } else if (javaType == long.class) {
                        pd = ProductData.createInstance((long[]) data);
                    } else if (javaType == float.class) {
                        pd = ProductData.createInstance((float[]) data);
                    } else if (javaType == String.class) {
                        final String[] strings = (String[]) data;
                        for (int i = 0; i < strings.length; i++) {
                            String string = strings[i];
                            final ProductData stringData = ProductData.createInstance(string);
                            arrayAttElem.addAttribute(new MetadataAttribute(attName + "." + (i + 1), stringData, true));
                        }
                        element.addElement(arrayAttElem);
                    } else {
                        throw new IllegalFileFormatException("Unsupported data type: " + data.getClass().getCanonicalName());
                    }
                } else {
                    if (data instanceof int[][]) {
                        final int[][] ints = (int[][]) data;
                        for (int i = 0; i < ints.length; i++) {
                            final String metaName = String.format("%s.%d", attName, i + 1);
                            arrayAttElem.addAttribute(new MetadataAttribute(metaName, ProductData.createInstance(ints[i]), true));
                        }
                        if (arrayAttElem.getNumAttributes() > 0) {
                            element.addElement(arrayAttElem);
                        }
                    } else {
                        throw new IllegalFileFormatException("Unsupported dimensions: " + Arrays.toString(dimensions));
                    }
                }
                if (pd != null) {
                    final MetadataAttribute ma = new MetadataAttribute(attName, pd, true);
                    arrayAttElem.addAttribute(ma);
                    element.addElement(arrayAttElem);
                }
            }
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

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
//            } else if (node instanceof Dataset) {
//                final Dataset dataset = (Dataset) node;
//                if (dataset.getName().equalsIgnoreCase(name)) {
//                    return dataset;
//                }
            }
        }
        return null;
    }

}
