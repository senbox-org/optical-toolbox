package eu.esa.opt.dataio.s3;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.dddb.DDDB;
import eu.esa.opt.dataio.s3.dddb.ProductDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableType;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.snap.dataio.netcdf.util.ReaderUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ucar.ma2.*;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sentinel3DDDBReader extends AbstractProductReader {

    private final DDDB dddb;
    private final Map<String, VariableDescriptor> tiepointMap;
    private final Map<String, VariableDescriptor> metadataMap;
    private VirtualDir virtualDir;
    private Map<String, VariableDescriptor> variablesMap;
    private Map<String, NetcdfFile> filesMap;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected Sentinel3DDDBReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        virtualDir = null;
        dddb = DDDB.getInstance();
        variablesMap = new HashMap<>();
        tiepointMap = new HashMap<>();
        metadataMap = new HashMap<>();
        filesMap = new HashMap<>();
    }

    // package access for testing only tb 2024-12-17
    static void ensureWidthAndHeight(ProductDescriptor productDescriptor, Manifest manifest) {
        int width = productDescriptor.getWidth();
        int height = productDescriptor.getHeight();
        if (width < 0 || height < 0) {
            // read from XPath
            width = manifest.getXPathInt(productDescriptor.getWidthXPath());
            productDescriptor.setWidth(width);
            height = manifest.getXPathInt(productDescriptor.getHeightXPath());
            productDescriptor.setHeight(height);
        }
    }

    private static VirtualDir getVirtualDir(Path inputPath) {
        VirtualDir virtualDir;
        if (isZipFile(inputPath)) {
            virtualDir = VirtualDir.create(inputPath);
        } else {
            Path productDirectory = inputPath;
            if (!Files.isDirectory(inputPath)) {
                productDirectory = inputPath.getParent();
            }
            virtualDir = VirtualDir.create(productDirectory);
        }

        return virtualDir;
    }

    // @todo 2 tb/tb this one wants to be member of a utility class 2024-12-06
    static boolean isZipFile(Path inputPath) {
        final Path fileName = inputPath.getFileName();
        return fileName.toString().toLowerCase().endsWith(".zip");
    }

    // @todo 1 tb/tb it's duplicated - move to an appropriate location and remove other code segment 2024-12-09
    // @todo 3 tb/tb mock test 2024-05-31
    private static InputStream getManifestInputStream(VirtualDir virtualDir) throws IOException {
        final String[] list = virtualDir.listAllFiles();
        for (final String entry : list) {
            if (entry.toLowerCase().endsWith(XfduManifest.MANIFEST_FILE_NAME)) {
                return virtualDir.getInputStream(entry);
            }
        }

        return null;
    }

    static String createDescriptorKey(VariableDescriptor descriptor) {
        return descriptor.getName();
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        initalizeInput();

        final Manifest manifest = readManifest();

        // create a naked produt, requires
        // - name
        final String productName = manifest.getProductName();
        // - type
        final String productType = manifest.getProductType();
        // - dimension
        final String baselineCollection = manifest.getBaselineCollection();
        final ProductDescriptor productDescriptor = dddb.getProductDescriptor(productType, baselineCollection);
        ensureWidthAndHeight(productDescriptor, manifest);

        final Product product = new Product(productName, productType, productDescriptor.getWidth(), productDescriptor.getHeight(), this);

        // add other properties from manifest and descriptor
        product.setDescription(manifest.getDescription());
        product.setFileLocation(new File(getInput().toString()));

        initializeDDDBDescriptors(manifest, productDescriptor);

        for (VariableDescriptor descriptor : variablesMap.values()) {
            // add band - check for other attributes (scaling/offset etc) tb 2025-12-18
            final int dataType = ProductData.getType(descriptor.getDataType());
            product.addBand(descriptor.getName(), dataType);
        }

        return product;
    }

    private void initializeDDDBDescriptors(Manifest manifest, ProductDescriptor productDescriptor) throws IOException {
        final String productType = manifest.getProductType();
        final String baselineCollection = manifest.getBaselineCollection();

        final List<String> fileNames = manifest.getFileNames(productDescriptor.getExcludedIdsAsArray());
        for (final String fileName : fileNames) {
            final VariableDescriptor[] variableDescriptors = dddb.getVariableDescriptors(fileName, productType, baselineCollection);
            for (final VariableDescriptor descriptor : variableDescriptors) {
                descriptor.setFileName(fileName);

                final String descriptorKey = createDescriptorKey(descriptor);
                final char type = descriptor.getType();

                final VariableType variableType = VariableType.fromChar(type);
                if (variableType == VariableType.VARIABLE || variableType == VariableType.FLAG) {
                    variablesMap.put(descriptorKey, descriptor);
                } else if (variableType == VariableType.TIE_POINT) {
                    // add TiePoint raster tb 2025-12-18
                    tiepointMap.put(descriptorKey, descriptor);
                } else if (variableType == VariableType.METADATA) {
                    // add metadatum as special metanode
                    // implement lazy loading for those file-based meta attributes tb 2025-12-18
                    metadataMap.put(descriptorKey, descriptor);
                }
            }
        }
    }

    private void initalizeInput() {
        final Path inputPath = getInputPath();
        virtualDir = getVirtualDir(inputPath);
    }

    private Manifest readManifest() throws IOException {
        final Manifest manifest;
        try (InputStream manifestInputStream = getManifestInputStream(virtualDir)) {
            final Document manifestDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestInputStream);
            manifest = XfduManifest.createManifest(manifestDocument);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException(e);
        }
        return manifest;
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        final String name = destBand.getName();
        final VariableDescriptor descriptor = variablesMap.get(name);

        String fileName = descriptor.getFileName();
        NetcdfFile netcdfFile = filesMap.get(fileName);
        if (netcdfFile == null) {
            final File file = virtualDir.getFile(fileName);
            if (file == null) {
                throw new IOException("File not found: " + fileName);
            }

            netcdfFile = NetcdfFileOpener.open(file.getAbsolutePath());
            filesMap.put(fileName, netcdfFile);
        }

        final Variable variable = netcdfFile.findVariable(name);

        final double scaleFactor = ReaderUtils.getScalingFactor(variable);
        final double offset = ReaderUtils.getAddOffset(variable);

        final int[] sliceOffset = {sourceOffsetY, sourceOffsetX};
        final int[] sliceDimensions = {sourceHeight, sourceWidth};
        final int[] stride = {sourceStepY, sourceStepX};
        try {
            final Section sect = new Section(sliceOffset, sliceDimensions, stride);
            Array sliceData;
            synchronized (netcdfFile) {
                sliceData = variable.read(sect);
            }
            if (ReaderUtils.mustScale(scaleFactor, offset)) {
                final MAMath.ScaleOffset scaleOffset = new MAMath.ScaleOffset(scaleFactor, offset);
                sliceData = MAMath.convert2Unpacked(sliceData, scaleOffset);
            }

            destBuffer.setElems(sliceData.get1DJavaArray(DataType.FLOAT));
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void readTiePointGridRasterData(TiePointGrid tpg, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        super.readTiePointGridRasterData(tpg, destOffsetX, destOffsetY, destWidth, destHeight, destBuffer, pm);
    }

    @Override
    public void close() throws IOException {
        if (virtualDir != null) {
            virtualDir.close();
            virtualDir = null;
        }
        if (variablesMap != null) {
            variablesMap.clear();
            variablesMap = null;
        }
        if (filesMap != null) {
            for (final NetcdfFile ncFile : filesMap.values()) {
                ncFile.close();
            }
            filesMap.clear();
            filesMap = null;
        }
        super.close();
    }

    private Path getInputPath() {
        // @todo 3 tb/tb check for null 2024-12-06
        return Paths.get(getInput().toString());
    }
}
