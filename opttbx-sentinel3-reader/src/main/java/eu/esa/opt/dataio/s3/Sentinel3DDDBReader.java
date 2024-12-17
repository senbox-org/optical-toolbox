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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Sentinel3DDDBReader extends AbstractProductReader {

    private final DDDB dddb;
    private VirtualDir virtualDir;

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

        final List<String> fileNames = manifest.getFileNames(productDescriptor.getExcludedIdsAsArray());
        for (final String fileName : fileNames) {
            final VariableDescriptor[] variableDescriptors = dddb.getVariableDescriptors(fileName, productType, baselineCollection);
            for (final VariableDescriptor descriptor : variableDescriptors) {
                final char type = descriptor.getType();
                final VariableType variableType = VariableType.fromChar(type);
                if (variableType == VariableType.VARIABLE || variableType == VariableType.FLAG) {
                    final int dataType = ProductData.getType(descriptor.getDataType());
                    product.addBand(descriptor.getName(), dataType);
                }
            }
        }

        return product;
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
        throw new RuntimeException("not implemented");
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
        super.close();
    }

    private Path getInputPath() {
        // @todo 3 tb/tb check for null 2024-12-06
        return Paths.get(getInput().toString());
    }
}
