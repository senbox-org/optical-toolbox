package eu.esa.opt.dataio.s3;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.dddb.DDDB;
import eu.esa.opt.dataio.s3.dddb.ProductDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableDescriptor;
import eu.esa.opt.dataio.s3.dddb.VariableType;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.ManifestUtil;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.snap.dataio.netcdf.util.ReaderUtils;
import org.esa.snap.engine_utilities.util.ZipUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
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
import java.util.TreeMap;

public class Sentinel3DDDBReader extends AbstractProductReader {

    private final DDDB dddb;
    private final Map<String, VariableDescriptor> tiepointMap;
    private final Map<String, VariableDescriptor> metadataMap;
    private final Map<String, VariableDescriptor> variablesMap;
    private final Map<String, Variable> ncVariablesMap;
    private final Map<String, NetcdfFile> filesMap;
    private final Map<String, Array> dataMap;
    private final Map<String, Float> nameToWavelengthMap;
    private final Map<String, Float> nameToBandwidthMap;
    private final Map<String, Integer> nameToIndexMap;
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
        // the treemap sorts the entries alphanumerically - as these data should be displayed in the SNAP product tree tb 2025-02-04
        variablesMap = new TreeMap<>();
        tiepointMap = new TreeMap<>();
        ncVariablesMap = new HashMap<>();
        metadataMap = new HashMap<>();
        filesMap = new HashMap<>();
        dataMap = new HashMap<>();
        nameToWavelengthMap = new HashMap<>();
        nameToBandwidthMap = new HashMap<>();
        nameToIndexMap = new HashMap<>();
    }

    // package access for testing only tb 2024-12-17
    static void ensureWidthAndHeight(ProductDescriptor productDescriptor, Manifest manifest) {
        int width = productDescriptor.getWidth();
        int height = productDescriptor.getHeight();
        if (width < 0 || height < 0) {
            width = manifest.getXPathInt(productDescriptor.getWidthXPath());
            productDescriptor.setWidth(width);
            height = manifest.getXPathInt(productDescriptor.getHeightXPath());
            productDescriptor.setHeight(height);
        }
    }

    private static VirtualDir getVirtualDir(Path inputPath) {
        VirtualDir virtualDir;
        if (ZipUtils.isZip(inputPath)) {
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

    static String createDescriptorKey(VariableDescriptor descriptor) {
        return descriptor.getName();
    }

    // @todo 2 tb/tb add tests for this 2025-02-04
    private static void extractSubset(RasterExtract rasterExtract, ProductData destBuffer, Array rawDataArray, Variable netCDFVariable) throws IOException {
        final int[] sliceOffset = {rasterExtract.getYOffset(), rasterExtract.getXOffset()};
        final int[] sliceDimensions = {rasterExtract.getHeight(), rasterExtract.getWidth()};
        final int[] stride = {rasterExtract.getStepY(), rasterExtract.getStepX()};

        try {
            Array sliceData = rawDataArray.section(sliceOffset, sliceDimensions, stride);
            sliceData = ReaderUtils.scaleArray(sliceData, netCDFVariable);
            assignResultData(destBuffer, sliceData);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    // package access for testing only tb 2025-02-04
    static void assignResultData(ProductData destBuffer, Array sliceData) {
        final int destDataType = destBuffer.getType();
        final DataType netcdfDataType = DataTypeUtils.getNetcdfDataType(destDataType);
        destBuffer.setElems(sliceData.get1DJavaArray(netcdfDataType));
    }

    static String bandNameToKey(String bandName) {
        // @todo 1 tb/tb this is OLCI specific - extract sensor specific class 2025-01-06
        return bandName.substring(0, 4);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        initalizeInput();

        final Manifest manifest = readManifest();
        MetadataElement metadata = manifest.getMetadata();
        MetadataElement metadataSection = metadata.getElement("metadataSection");
        // @todo 2 tb this is OLCI specific - extract generic functionality and dispatch to product specific implementation 2025-01-06
        MetadataElement olciProductInformation = metadataSection.getElement("olciProductInformation");

        MetadataElement bandDescriptionsElement = olciProductInformation.getElement("bandDescriptions");
        // @todo 1 tb/tb duplicated, other segment is in OlciProductFactory 2024-12-20
        if (bandDescriptionsElement != null) {
            for (int i = 0; i < bandDescriptionsElement.getNumElements(); i++) {
                final MetadataElement bandDescriptionElement = bandDescriptionsElement.getElementAt(i);
                final String bandKey = bandDescriptionElement.getAttribute("name").getData().getElemString();
                final float wavelength = Float.parseFloat(bandDescriptionElement.getAttribute("centralWavelength").getData().getElemString());
                final float bandWidth = Float.parseFloat(bandDescriptionElement.getAttribute("bandWidth").getData().getElemString());
                nameToWavelengthMap.put(bandKey, wavelength);
                nameToBandwidthMap.put(bandKey, bandWidth);
                nameToIndexMap.put(bandKey, i);
            }
        }

        // create a naked produt
        final String productName = manifest.getProductName();
        final String productType = manifest.getProductType();
        final String baselineCollection = manifest.getBaselineCollection();
        final ProductDescriptor productDescriptor = dddb.getProductDescriptor(productType, baselineCollection);
        ensureWidthAndHeight(productDescriptor, manifest);

        final Product product = new Product(productName, productType, productDescriptor.getWidth(), productDescriptor.getHeight(), this);

        // add other properties from manifest and descriptor
        product.setDescription(manifest.getDescription());
        product.setFileLocation(new File(getInput().toString()));
        product.setAutoGrouping(productDescriptor.getBandGroupingPattern());

        initializeDDDBDescriptors(manifest, productDescriptor);
        addVariables(product);
        addTiePointGrids(product, manifest);

        return product;
    }

    private void addVariables(Product product) {
        for (VariableDescriptor descriptor : variablesMap.values()) {
            // @todo 2 - add band - check for other attributes (unit, description, fill value) tb 2025-12-18
            final int dataType = ProductData.getType(descriptor.getDataType());
            final String bandname = descriptor.getName();
            final Band band = product.addBand(bandname, dataType);

            final String bandKey = bandNameToKey(bandname);
            if (nameToWavelengthMap.containsKey(bandKey)) {
                band.setSpectralWavelength(nameToWavelengthMap.get(bandKey));
            }
            if (nameToBandwidthMap.containsKey(bandKey)) {
                band.setSpectralBandwidth(nameToBandwidthMap.get(bandKey));
            }
            if (nameToIndexMap.containsKey(bandKey)) {
                band.setSpectralBandIndex(nameToIndexMap.get(bandKey));
            }
        }
    }

    private void addTiePointGrids(Product product, Manifest manifest) {
        for (VariableDescriptor descriptor : tiepointMap.values()) {
            final String tiePointName = descriptor.getName();
            final int subsamplingX = manifest.getXPathInt(descriptor.getTpXSubsamplingXPath());
            final int subsamplingY = manifest.getXPathInt(descriptor.getTpYSubsamplingXPath());

            final int tpRasterWidth = (int) Math.ceil((double) product.getSceneRasterWidth() / subsamplingX);
            final int tpRasterHeight = (int) Math.ceil((double) product.getSceneRasterHeight() / subsamplingY);

            final TiePointGrid tiePointGrid = new TiePointGrid(tiePointName, tpRasterWidth, tpRasterHeight, 0.5, 0.5, subsamplingX, subsamplingY);
            product.addTiePointGrid(tiePointGrid);
        }
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

    // @todo 3 tb/tb this could be made testable by passing in the virtual dir. We can mock then. 2025-02-03
    private Manifest readManifest() throws IOException {
        final Manifest manifest;
        try (InputStream manifestInputStream = ManifestUtil.getManifestInputStream(virtualDir)) {
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

        final RasterExtract rasterExtract = new RasterExtract(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY);
        readData(rasterExtract, destBuffer, descriptor, name);
    }

    @Override
    public void readTiePointGridRasterData(TiePointGrid tpg, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        final String tpGridName = tpg.getName();
        final VariableDescriptor descriptor = tiepointMap.get(tpGridName);

        final RasterExtract rasterExtract = new RasterExtract(destOffsetX, destOffsetY, destWidth, destHeight, 1, 1);
        readData(rasterExtract, destBuffer, descriptor, tpGridName);
    }

    private void readData(RasterExtract rasterExtract, ProductData destBuffer, VariableDescriptor descriptor, String name) throws IOException {
        final Variable netCDFVariable = getNetCDFVariable(descriptor, name);
        final Array rawDataArray = getRawData(name, netCDFVariable);

        extractSubset(rasterExtract, destBuffer, rawDataArray, netCDFVariable);
    }

    private Variable getNetCDFVariable(VariableDescriptor descriptor, String name) throws IOException {
        final NetcdfFile netcdfFile = getNetcdfFile(descriptor.getFileName());

        String variableName = name;
        final String ncVarName = descriptor.getNcVarName();
        if (StringUtils.isNotNullAndNotEmpty(ncVarName)) {
            variableName = ncVarName;
        }

        Variable ncVar;

        synchronized (ncVariablesMap) {
            Variable variable;

            variable = ncVariablesMap.get(name);
            if (variable == null) {
                variable = netcdfFile.findVariable(variableName);
                if (variable == null) {
                    throw new IOException("requested variable not found: " + name + netcdfFile.getLocation());
                }
                ncVariablesMap.put(name, variable);
            }

            ncVar = variable;
        }
        return ncVar;
    }

    private Array getRawData(String name, Variable variable) throws IOException {
        Array fullDataArray;

        synchronized (dataMap) {
            fullDataArray = dataMap.get(name);
            if (fullDataArray == null) {
                fullDataArray = variable.read();
                dataMap.put(name, fullDataArray);
            }
        }
        return fullDataArray;
    }

    private NetcdfFile getNetcdfFile(String fileName) throws IOException {
        NetcdfFile netcdfFile;
        synchronized (filesMap) {
            netcdfFile = filesMap.get(fileName);
            if (netcdfFile == null) {
                final File file = virtualDir.getFile(fileName);
                if (file == null) {
                    throw new IOException("File not found: " + fileName);
                }

                netcdfFile = NetcdfFileOpener.open(file.getAbsolutePath());
                filesMap.put(fileName, netcdfFile);
            }
        }
        return netcdfFile;
    }

    @Override
    public void close() throws IOException {
        if (virtualDir != null) {
            virtualDir.close();
            virtualDir = null;
        }
        variablesMap.clear();
        ncVariablesMap.clear();
        tiepointMap.clear();

        for (final NetcdfFile ncFile : filesMap.values()) {
            ncFile.close();
        }
        filesMap.clear();
        dataMap.clear();

        super.close();
    }

    private Path getInputPath() {
        return Paths.get(getInput().toString());
    }
}
