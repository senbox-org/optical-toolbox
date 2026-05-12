package eu.esa.opt.dataio.flex;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.flex.compatibility.EarlyProcessorCompatibility;
import eu.esa.opt.dataio.flex.compatibility.FlexProductCompatibility;
import eu.esa.opt.dataio.flex.compatibility.StandardFlexCompatibility;
import eu.esa.opt.dataio.flex.dddb.*;
import eu.esa.opt.dataio.flex.metadata.FlexMetadataElementLazy;
import eu.esa.opt.dataio.flex.metadata.FlexMetadataProvider;
import eu.esa.opt.dataio.flex.header.FlexHeaderParser;
import eu.esa.opt.dataio.flex.header.FlexProductHeader;
import eu.esa.snap.core.dataio.cache.CacheManager;
import eu.esa.snap.core.dataio.cache.CachedSubsamplingReader;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import eu.esa.snap.core.datamodel.band.BandUsingReaderDirectly;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.image.ImageManager;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.dataio.netcdf.cache.NetcdfCacheDataProvider;
import org.esa.snap.dataio.netcdf.util.ArrayConverter;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class FlexProductReader extends AbstractProductReader implements FlexMetadataProvider {

    private static final Logger logger = Logger.getLogger(FlexProductReader.class.getName());
    private static final String LATITUDE_BAND_NAME = "latitude";
    private static final String LONGITUDE_BAND_NAME = "longitude";
    private static final double FLEX_RESOLUTION_KM = 0.3;
    private static final String DIM_ACROSS_TRACK = "number_of_across_track_samples";
    private static final String DIM_ALONG_TRACK = "number_of_along_track_samples";

    private final FlexDDDB dddb;
    private final Map<String, FlexVariableDescriptor> variablesMap;
    private final Map<String, FlexVariableDescriptor> flagsMap;
    private final Map<String, FlexVariableDescriptor> specialsMap;
    private final Map<String, FlexVariableDescriptor> metadataMap;
    private final Map<String, NetcdfFile> ncFilesMap;
    private final Map<String, Variable> ncVariablesCache;
    private final Map<String, String> descriptorToFileMap;

    private String dddbProductType;
    private VirtualDir virtualDir;
    private FlexProductCompatibility compatibility;
    private NetcdfCacheDataProvider cacheDataProvider;
    private ProductCache productCache;

    FlexProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        dddb = FlexDDDB.getInstance();
        variablesMap = new TreeMap<>();
        flagsMap = new TreeMap<>();
        specialsMap = new TreeMap<>();
        metadataMap = new HashMap<>();
        ncFilesMap = new HashMap<>();
        ncVariablesCache = new HashMap<>();
        descriptorToFileMap = new HashMap<>();
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final Path inputPath = getProductPath();
        virtualDir = ProductUtils.getProductVirtualDir(inputPath);

        cacheDataProvider = new NetcdfCacheDataProvider();
        productCache = new ProductCache(cacheDataProvider);
        CacheManager.getInstance().register(productCache);

        final Path headerFile = findHeaderFile(inputPath);
        final FlexHeaderParser parser = new FlexHeaderParser();
        final FlexProductHeader header = parser.parse(headerFile);

        dddbProductType = mapProductType(header.getProductType());
        compatibility = detectCompatibility(header);

        final FlexProductDescriptor productDescriptor = dddb.getProductDescriptor(dddbProductType);

        openNcFiles(header);
        loadDescriptors(productDescriptor, dddbProductType);

        final int width = resolveProductWidth(productDescriptor);
        final int height = resolveProductHeight(productDescriptor);

        final Product product = new Product(header.getProductName(), dddbProductType,
                width, height, this);

        product.setFileLocation(inputPath.toFile());
        product.setAutoGrouping(productDescriptor.getBandGroupingPattern());
        setProductTimes(product, header);

        addBands(product, productDescriptor);
        addSpecialBands(product);
        addFlagMasks(product, productDescriptor);
        addMetadata(product, header);
        addMetadataVariables(product);

        return product;
    }

    @Override
    public boolean isSubsetReadingFullySupported() {
        return true;
    }

    @Override
    public GeoCoding readGeoCoding(Product product) throws IOException {
        Band lonBand = product.getBand(LONGITUDE_BAND_NAME);
        Band latBand = product.getBand(LATITUDE_BAND_NAME);
        if (lonBand == null || latBand == null) {
            for (final String prefix : new String[]{"LRES_", "HRE1_", "HRE2_"}) {
                lonBand = product.getBand(prefix + LONGITUDE_BAND_NAME);
                latBand = product.getBand(prefix + LATITUDE_BAND_NAME);
                if (lonBand != null && latBand != null) {
                    break;
                }
            }
            if (lonBand == null || latBand == null) {
                return null;
            }
        }

        final int width = lonBand.getRasterWidth();
        final int height = lonBand.getRasterHeight();

        final String lonBandName = lonBand.getName();
        final String latBandName = latBand.getName();
        final double[] lonData = readGeoData(lonBandName, width, height, lonBand);
        final double[] latData = readGeoData(latBandName, width, height, latBand);

        final GeoRaster geoRaster = new GeoRaster(lonData, latData,
                lonBandName, latBandName,
                width, height, FLEX_RESOLUTION_KM);

        final ForwardCoding forward = ComponentFactory.getForward(PixelForward.KEY);
        final InverseCoding inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        return geoCoding;
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY, Band destBand, int destOffsetX,
                                          int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        CachedSubsamplingReader.read(productCache, destBand.getName(), destBand.getDataType(),
                sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight,
                sourceStepX, sourceStepY, destWidth, destHeight, destBuffer);
    }

    @Override
    public void close() throws IOException {
        if (productCache != null) {
            CacheManager.getInstance().remove(productCache);
            productCache = null;
        }
        cacheDataProvider = null;

        variablesMap.clear();
        flagsMap.clear();
        specialsMap.clear();
        metadataMap.clear();
        ncVariablesCache.clear();
        descriptorToFileMap.clear();

        for (final NetcdfFile ncFile : ncFilesMap.values()) {
            ncFile.close();
        }
        ncFilesMap.clear();

        if (virtualDir != null) {
            virtualDir.close();
            virtualDir = null;
        }

        dddbProductType = null;
        compatibility = null;

        super.close();
    }

    // package access for testing
    static String mapProductType(String headerProductType) {
        if (headerProductType.contains("L1B") && headerProductType.contains("OBS")) {
            return "FLX_L1B_OBS";
        } else if (headerProductType.contains("L1C") && headerProductType.contains("FLXSYN")) {
            return "FLX_L1C_FLXSYN";
        } else if (headerProductType.contains("L2") && headerProductType.contains("FLXSYN")) {
            return "FLX_L2_FLXSYN";
        }
        throw new IllegalArgumentException("Unknown FLEX product type: " + headerProductType);
    }

    private double[] readGeoData(String bandName, int width, int height, Band band) throws IOException {
        final int sourceDataType = band.getDataType();
        final int[] offsets = {0, 0};
        final int[] shapes = {height, width};

        final DataBuffer buffer = new DataBuffer(sourceDataType, offsets, shapes);
        productCache.read(bandName, offsets, shapes, buffer);

        final ProductData sourceData = buffer.getData();
        final double scaleFactor = band.getScalingFactor();
        final double scaleOffset = band.getScalingOffset();

        final double[] data = new double[width * height];
        for (int i = 0; i < data.length; i++) {
            data[i] = sourceData.getElemDoubleAt(i) * scaleFactor + scaleOffset;
        }
        return data;
    }

    private int resolveProductWidth(FlexProductDescriptor productDescriptor) {
        final String groupPath = findFirstGroupPath();
        for (final NetcdfFile ncFile : ncFilesMap.values()) {
            final int resolved = compatibility.resolveDimension(ncFile, groupPath, DIM_ACROSS_TRACK, productDescriptor.getWidth());

            if (resolved != productDescriptor.getWidth()) {
                logger.fine("Resolved width from netCDF: " + resolved + " (spec default: " + productDescriptor.getWidth() + ")");
            }
            return resolved;
        }
        return productDescriptor.getWidth();
    }

    private int resolveProductHeight(FlexProductDescriptor productDescriptor) {
        final String groupPath = findFirstGroupPath();
        for (final NetcdfFile ncFile : ncFilesMap.values()) {
            final int resolved = compatibility.resolveDimension(ncFile, groupPath, DIM_ALONG_TRACK, productDescriptor.getHeight());

            if (resolved != productDescriptor.getHeight()) {
                logger.fine("Resolved height from netCDF: " + resolved + " (spec default: " + productDescriptor.getHeight() + ")");
            }
            return resolved;
        }
        return productDescriptor.getHeight();
    }

    private String findFirstGroupPath() {
        for (final FlexVariableDescriptor descriptor : variablesMap.values()) {
            if (descriptor.getNcGroupPath() != null && !descriptor.getNcGroupPath().isEmpty()) {
                return descriptor.getNcGroupPath();
            }
        }
        for (final FlexVariableDescriptor descriptor : specialsMap.values()) {
            if (descriptor.getNcGroupPath() != null && !descriptor.getNcGroupPath().isEmpty()) {
                return descriptor.getNcGroupPath();
            }
        }
        return "";
    }

    private Path findHeaderFile(Path inputPath) throws IOException {
        final File inputFile = inputPath.toFile();
        final Path productDir;
        if (inputFile.isFile()) {
            if (inputFile.getName().toLowerCase().endsWith(".xml")) {
                return inputPath;
            }
            productDir = inputPath.getParent();
        } else {
            productDir = inputPath;
        }

        try (Stream<Path> files = Files.list(productDir)) {
            return files
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                    .findFirst()
                    .orElseThrow(() -> new IOException("No XML header file found in " + productDir));
        }
    }

    private FlexProductCompatibility detectCompatibility(FlexProductHeader header) {
        for (final String fileName : header.getDataFileNames()) {
            if (fileName.endsWith(".nc.nc")) {
                return new EarlyProcessorCompatibility();
            }
        }
        return new StandardFlexCompatibility();
    }

    private void openNcFiles(FlexProductHeader header) throws IOException {
        final List<String> dataFileNames = header.getDataFileNames();
        for (final String headerFileName : dataFileNames) {
            final String resolvedName = compatibility.resolveDataFilePath(headerFileName);
            final String ncKey = resolvedName.toLowerCase();

            if (ncFilesMap.containsKey(ncKey)) {
                continue;
            }

            try {
                final File file = virtualDir.getFile(resolvedName);
                if (file != null && file.exists()) {
                    final NetcdfFile ncFile = NetcdfFileOpener.open(file);
                    ncFilesMap.put(ncKey, ncFile);
                    logger.fine("Opened data file: " + resolvedName + " (" + ncFile.getVariables().size() + " variables)");
                } else {
                    logger.fine("Data file not found: " + resolvedName);
                }
            } catch (IOException e) {
                logger.warning("Cannot open data file: " + resolvedName + " - " + e.getMessage());
            }
        }

        if (ncFilesMap.isEmpty()) {
            openAllNcFilesInDirectory();
        }
    }

    private void openAllNcFilesInDirectory() throws IOException {
        final String[] files = virtualDir.list("");
        if (files == null) {
            return;
        }

        for (final String fileName : files) {
            if (fileName.toLowerCase().endsWith(".nc")) {
                try {
                    final File file = virtualDir.getFile(fileName);
                    if (file != null && file.exists()) {
                        ncFilesMap.put(fileName.toLowerCase(), NetcdfFileOpener.open(file));
                    }
                } catch (IOException e) {
                    logger.warning("Cannot open: " + fileName + " - " + e.getMessage());
                }
            }
        }
    }

    private void setProductTimes(Product product, FlexProductHeader header) {
        final String startTime = header.getStartTime();
        if (!startTime.isEmpty()) {
            try {
                String normalizedStartTime = startTime.replace("Z", "");
                product.setStartTime(ProductData.UTC.parse(normalizedStartTime, "yyyy-MM-dd'T'HH:mm:ss"));
            } catch (ParseException e) {
                logger.warning("Cannot parse start time: " + startTime);
            }
        }
        final String stopTime = header.getStopTime();
        if (!stopTime.isEmpty()) {
            try {
                String normalizedStartTime = startTime.replace("Z", "");
                product.setEndTime(ProductData.UTC.parse(normalizedStartTime, "yyyy-MM-dd'T'HH:mm:ss"));
            } catch (ParseException e) {
                logger.warning("Cannot parse stop time: " + stopTime);
            }
        }
    }

    private void loadDescriptors(FlexProductDescriptor productDescriptor, String productType) throws IOException {
        for (final String dataFile : productDescriptor.getDataFiles()) {
            final FlexVariableDescriptor[] descriptors = dddb.getVariableDescriptors(dataFile, productType);
            for (final FlexVariableDescriptor descriptor : descriptors) {
                final String name = descriptor.getName();
                descriptorToFileMap.put(name, dataFile);

                final FlexVariableType varType = descriptor.getVariableType();
                switch (varType) {
                    case VARIABLE, TIE_POINT -> variablesMap.put(name, descriptor);
                    case FLAG, BITMASK_FLAG -> flagsMap.put(name, descriptor);
                    case SPECIAL -> specialsMap.put(name, descriptor);
                    case METADATA -> metadataMap.put(name, descriptor);
                }
            }
        }
    }

    private void addBands(Product product, FlexProductDescriptor productDescriptor) throws IOException {
        for (final FlexVariableDescriptor descriptor : variablesMap.values()) {
            addBand(product, descriptor);
        }
        for (final FlexVariableDescriptor descriptor : flagsMap.values()) {
            if (descriptor.getVariableType() == FlexVariableType.BITMASK_FLAG) {
                addBitmaskFlagBand(product, descriptor, productDescriptor);
            } else {
                addFlagBand(product, descriptor, productDescriptor);
            }
        }
    }

    private void addBand(Product product, FlexVariableDescriptor descriptor) {
        final int dataType = ProductData.getType(descriptor.getDataType());
        final String bandName = descriptor.getName();

        final Variable ncVariable = findNcVariable(descriptor);
        if (ncVariable == null) {
            if (!descriptor.isOptional()) {
                logger.warning("Variable not found: " + descriptor.getFullNcPath());
            }
            return;
        }

        final Band band = new BandUsingReaderDirectly(bandName, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());

        band.setDescription(descriptor.getDescription());
        band.setUnit(descriptor.getUnits());
        setScaleAndOffset(band, ncVariable);
        setFillValue(band, ncVariable);
        product.addBand(band);

        final Dimension tileSize = ImageManager.getPreferredTileSize(product);
        cacheDataProvider.register(bandName, ncVariable, new int[0], false, dataType, ArrayConverter.IDENTITY, tileSize);
    }

    private void addFlagBand(Product product, FlexVariableDescriptor descriptor,
                             FlexProductDescriptor productDescriptor) {
        final int dataType = ProductData.getType(descriptor.getDataType());
        final String bandName = descriptor.getName();

        final Variable ncVariable = findNcVariable(descriptor);
        if (ncVariable == null) {
            if (!descriptor.isOptional()) {
                logger.warning("Flag variable not found: " + descriptor.getFullNcPath());
            }
            return;
        }

        final Band band = new BandUsingReaderDirectly(bandName, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        band.setDescription(descriptor.getDescription());

        final IndexCoding indexCoding = new IndexCoding(bandName);
        for (final FlexFlagMask mask : productDescriptor.getFlagMasks()) {
            if (mask.getBandName().equals(bandName)) {
                indexCoding.addIndex(mask.getName(), mask.getValue(), mask.getDescription());
            }
        }
        if (indexCoding.getNumAttributes() > 0) {
            product.getIndexCodingGroup().add(indexCoding);
            band.setSampleCoding(indexCoding);
        }

        product.addBand(band);

        final Dimension tileSize = ImageManager.getPreferredTileSize(product);
        cacheDataProvider.register(bandName, ncVariable, new int[0], false, dataType, ArrayConverter.IDENTITY, tileSize);
    }

    private void addBitmaskFlagBand(Product product, FlexVariableDescriptor descriptor,
                                    FlexProductDescriptor productDescriptor) {
        final int dataType = ProductData.getType(descriptor.getDataType());
        final String bandName = descriptor.getName();

        final Variable ncVariable = findNcVariable(descriptor);
        if (ncVariable == null) {
            if (!descriptor.isOptional()) {
                logger.warning("Bitmask flag variable not found: " + descriptor.getFullNcPath());
            }
            return;
        }

        final Band band = new BandUsingReaderDirectly(bandName, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        band.setDescription(descriptor.getDescription());

        final FlagCoding flagCoding = new FlagCoding(bandName);
        for (final FlexFlagMask mask : productDescriptor.getFlagMasks()) {
            if (mask.getBandName().equals(bandName)) {
                flagCoding.addFlag(mask.getName(), mask.getValue(), mask.getDescription());
            }
        }
        if (flagCoding.getNumAttributes() > 0) {
            product.getFlagCodingGroup().add(flagCoding);
            band.setSampleCoding(flagCoding);
        }

        product.addBand(band);

        final Dimension tileSize = ImageManager.getPreferredTileSize(product);
        cacheDataProvider.register(bandName, ncVariable, new int[0], false, dataType, ArrayConverter.IDENTITY, tileSize);
    }

    private void addSpecialBands(Product product) {
        for (final FlexVariableDescriptor descriptor : specialsMap.values()) {
            final Variable ncVariable = findNcVariable(descriptor);
            if (ncVariable == null) {
                if (!descriptor.isOptional()) {
                    logger.warning("Special variable not found: " + descriptor.getFullNcPath());
                }
                continue;
            }

            final int depth = descriptor.getDepth();
            if (depth <= 0) {
                continue;
            }

            final int dataType = ProductData.getType(descriptor.getDataType());
            final String baseName = descriptor.getName();
            final String token = descriptor.getDepthPrefixToken();
            final Dimension tileSize = ImageManager.getPreferredTileSize(product);

            for (int layer = 1; layer <= depth; layer++) {
                final String layerBandName = baseName + token + layer;

                final Band band = new BandUsingReaderDirectly(layerBandName, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());

                band.setDescription(descriptor.getDescription());
                band.setUnit(descriptor.getUnits());
                setScaleAndOffset(band, ncVariable);
                setFillValue(band, ncVariable);
                product.addBand(band);

                cacheDataProvider.register(layerBandName, ncVariable, new int[]{layer - 1}, false, dataType, ArrayConverter.IDENTITY, tileSize);
            }
        }
    }

    private void addFlagMasks(Product product, FlexProductDescriptor productDescriptor) {
        int colorIndex = 0;
        for (final FlexFlagMask mask : productDescriptor.getFlagMasks()) {
            if (product.getBand(mask.getBandName()) == null) {
                continue;
            }
            final String expression;
            if (mask.isBitmask()) {
                expression = mask.getBandName() + " & " + mask.getValue() + " != 0";
            } else {
                expression = mask.getBandName() + " == " + mask.getValue();
            }
            final String maskName = mask.getBandName() + "_" + mask.getName();
            product.addMask(maskName, expression, mask.getDescription(),
                    MASK_COLORS[colorIndex++ % MASK_COLORS.length], 0.5);
        }
    }

    private void addMetadata(Product product, FlexProductHeader header) {
        final MetadataElement metadataRoot = product.getMetadataRoot();

        final MetadataElement headerElement = new MetadataElement("Header");
        addStringAttribute(headerElement, "productName", header.getProductName());
        addStringAttribute(headerElement, "productType", header.getProductType());
        addStringAttribute(headerElement, "startTime", header.getStartTime());
        addStringAttribute(headerElement, "stopTime", header.getStopTime());
        addStringAttribute(headerElement, "platformName", header.getPlatformName());
        addStringAttribute(headerElement, "instrumentName", header.getInstrumentName());
        if (header.getOrbitNumber() >= 0) {
            headerElement.addAttribute(new MetadataAttribute("orbitNumber",
                    ProductData.createInstance(new int[]{header.getOrbitNumber()}), true));
        }
        addStringAttribute(headerElement, "orbitDirection", header.getOrbitDirection());
        addStringAttribute(headerElement, "processorName", header.getProcessorName());
        addStringAttribute(headerElement, "processorVersion", header.getProcessorVersion());
        metadataRoot.addElement(headerElement);

        final Map<String, String> vendorSpecific = header.getVendorSpecific();
        if (!vendorSpecific.isEmpty()) {
            final MetadataElement vendorElement = new MetadataElement("VendorSpecific");
            for (final Map.Entry<String, String> entry : vendorSpecific.entrySet()) {
                addStringAttribute(vendorElement, entry.getKey(), entry.getValue());
            }
            metadataRoot.addElement(vendorElement);
        }
    }

    private void addMetadataVariables(Product product) {
        if (metadataMap.isEmpty()) {
            return;
        }
        final MetadataElement metadataRoot = product.getMetadataRoot();
        for (final FlexVariableDescriptor descriptor : metadataMap.values()) {
            final FlexMetadataElementLazy lazyElement = new FlexMetadataElementLazy(descriptor.getName(), this);
            metadataRoot.addElement(lazyElement);
        }
    }

    @Override
    public MetadataElement readElement(String name) throws IOException {
        final FlexVariableDescriptor descriptor = metadataMap.get(name);
        if (descriptor == null) {
            return null;
        }
        final Variable ncVariable = findNcVariable(descriptor);
        if (ncVariable == null) {
            return null;
        }
        return extractMetadata(ncVariable);
    }

    static MetadataElement extractMetadata(Variable variable) throws IOException {
        final MetadataElement element = new MetadataElement(variable.getFullName());

        for (final Attribute attribute : variable.getAttributes()) {
            if (attribute.getValues() != null) {
                final ProductData data = getAttributeData(attribute);
                if (data != null) {
                    element.addAttribute(new MetadataAttribute(attribute.getFullName(), data, true));
                }
            }
        }

        if (variable.getDataType() != DataType.STRING) {
            final Object data = variable.read().copyTo1DJavaArray();
            MetadataAttribute valueAttribute = null;
            if (data instanceof float[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((float[]) data), true);
            } else if (data instanceof double[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((double[]) data), true);
            } else if (data instanceof byte[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((byte[]) data), true);
            } else if (data instanceof short[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((short[]) data), true);
            } else if (data instanceof int[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((int[]) data), true);
            } else if (data instanceof long[]) {
                valueAttribute = new MetadataAttribute("value", ProductData.createInstance((long[]) data), true);
            }
            if (valueAttribute != null) {
                valueAttribute.setUnit(variable.getUnitsString());
                valueAttribute.setDescription(variable.getDescription());
                element.addAttribute(valueAttribute);
            }
        }

        return element;
    }

    private static ProductData getAttributeData(Attribute attribute) {
        final int type = DataTypeUtils.getEquivalentProductDataType(attribute.getDataType(), false, false);
        final ucar.ma2.Array values = attribute.getValues();
        switch (type) {
            case ProductData.TYPE_ASCII:
                return ProductData.createInstance(values.toString());
            case ProductData.TYPE_INT8:
                return ProductData.createInstance((byte[]) values.copyTo1DJavaArray());
            case ProductData.TYPE_INT16:
                return ProductData.createInstance((short[]) values.copyTo1DJavaArray());
            case ProductData.TYPE_INT32:
                return ProductData.createInstance((int[]) values.copyTo1DJavaArray());
            case ProductData.TYPE_INT64:
                return ProductData.createInstance((long[]) values.copyTo1DJavaArray());
            case ProductData.TYPE_FLOAT32:
                return ProductData.createInstance((float[]) values.copyTo1DJavaArray());
            case ProductData.TYPE_FLOAT64:
                return ProductData.createInstance((double[]) values.copyTo1DJavaArray());
            default:
                return null;
        }
    }

    private static void addStringAttribute(MetadataElement element, String name, String value) {
        element.addAttribute(new MetadataAttribute(name, ProductData.createInstance(value), true));
    }

    private Variable findNcVariable(FlexVariableDescriptor descriptor) {
        final String cacheKey = descriptor.getName();

        Variable cached = ncVariablesCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        final String fullPath = descriptor.getFullNcPath();
        final String ncDataFile = descriptor.getNcDataFile();

        for (final Map.Entry<String, NetcdfFile> entry : ncFilesMap.entrySet()) {
            if (!ncDataFile.isEmpty() && !entry.getKey().contains(ncDataFile)) {
                continue;
            }
            Variable variable = entry.getValue().findVariable(fullPath);
            if (variable != null) {
                ncVariablesCache.put(cacheKey, variable);
                return variable;
            }
        }

        final String groupPath = descriptor.getNcGroupPath();
        if (groupPath.contains("_")) {
            final String altPath = groupPath.replace("_", " ") + "/" + descriptor.getNcVarName();
            for (final Map.Entry<String, NetcdfFile> entry : ncFilesMap.entrySet()) {
                if (!ncDataFile.isEmpty() && !entry.getKey().contains(ncDataFile)) {
                    continue;
                }
                Variable variable = entry.getValue().findVariable(altPath);
                if (variable != null) {
                    ncVariablesCache.put(cacheKey, variable);
                    return variable;
                }
            }
        }

        return null;
    }

    private static void setScaleAndOffset(Band band, Variable ncVariable) {
        final Attribute scaleFactor = ncVariable.findAttribute("scale_factor");
        if (scaleFactor != null) {
            band.setScalingFactor(scaleFactor.getNumericValue().doubleValue());
        }
        final Attribute addOffset = ncVariable.findAttribute("add_offset");
        if (addOffset != null) {
            band.setScalingOffset(addOffset.getNumericValue().doubleValue());
        }
    }

    private static void setFillValue(Band band, Variable ncVariable) {
        final Attribute fillValue = ncVariable.findAttribute("_FillValue");
        if (fillValue != null) {
            band.setNoDataValue(fillValue.getNumericValue().doubleValue());
            band.setNoDataValueUsed(true);
        }
    }

    private static final Color[] MASK_COLORS = {
            new Color(0, 100, 0), new Color(0, 0, 200), new Color(200, 0, 0),
            new Color(200, 200, 0), new Color(0, 200, 200), new Color(200, 0, 200),
            new Color(100, 100, 0), new Color(0, 100, 100), new Color(100, 0, 100),
            new Color(128, 128, 128)
    };
}
