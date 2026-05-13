package eu.esa.opt.dataio.flex;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.flex.compatibility.FlexProductCompatibility;
import eu.esa.opt.dataio.flex.dddb.*;
import eu.esa.opt.dataio.flex.metadata.FlexMetadataElementLazy;
import eu.esa.opt.dataio.flex.metadata.FlexMetadataProvider;
import eu.esa.opt.dataio.flex.header.FlexHeaderParser;
import eu.esa.opt.dataio.flex.header.FlexProductHeader;
import eu.esa.opt.dataio.flex.util.FlexReaderUtils;
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
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;


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

    public static final String NETCDF_BASE_METADATA_ELEMENT = "NetCDF";


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

        final Path headerFile = FlexReaderUtils.findHeaderFile(inputPath);
        final FlexHeaderParser parser = new FlexHeaderParser();
        final FlexProductHeader header = parser.parse(headerFile);

        dddbProductType = FlexReaderUtils.mapProductType(header.getProductType());
        compatibility = FlexReaderUtils.detectCompatibility(header);

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

        addMetadata(product, header);
        addMetadataVariables(product);
        addBands(product, productDescriptor);
        addSpecialBands(product);
        addFlagMasks(product, productDescriptor);

        return product;
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
                    case VARIABLE -> variablesMap.put(name, descriptor);
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
        FlexReaderUtils.setScaleAndOffset(band, ncVariable);
        FlexReaderUtils.setFillValue(band, ncVariable);
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
                FlexReaderUtils.setScaleAndOffset(band, ncVariable);
                FlexReaderUtils.setFillValue(band, ncVariable);
                FlexReaderUtils.setSpectralWavelength(band, product, descriptor.getWavelengthReference(), layer);
                FlexReaderUtils.setSpectralFwhm(band, product, descriptor.getFwhmReference(), layer);
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
            product.addMask(maskName, expression, mask.getDescription(), MASK_COLORS[colorIndex++ % MASK_COLORS.length], 0.5);
        }
    }

    private static void addMetadata(Product product, FlexProductHeader header) {
        final MetadataElement metadataRoot = product.getMetadataRoot();

        final MetadataElement headerElement = new MetadataElement("Header");
        FlexReaderUtils.addStringAttribute(headerElement, "productName", header.getProductName());
        FlexReaderUtils.addStringAttribute(headerElement, "productType", header.getProductType());
        FlexReaderUtils.addStringAttribute(headerElement, "startTime", header.getStartTime());
        FlexReaderUtils.addStringAttribute(headerElement, "stopTime", header.getStopTime());
        FlexReaderUtils.addStringAttribute(headerElement, "platformName", header.getPlatformName());
        FlexReaderUtils.addStringAttribute(headerElement, "instrumentName", header.getInstrumentName());

        if (header.getOrbitNumber() >= 0) {
            headerElement.addAttribute(new MetadataAttribute("orbitNumber",
                    ProductData.createInstance(new int[]{header.getOrbitNumber()}), true));
        }

        FlexReaderUtils.addStringAttribute(headerElement, "orbitDirection", header.getOrbitDirection());
        FlexReaderUtils.addStringAttribute(headerElement, "processorName", header.getProcessorName());
        FlexReaderUtils.addStringAttribute(headerElement, "processorVersion", header.getProcessorVersion());
        metadataRoot.addElement(headerElement);

        final Map<String, String> vendorSpecific = header.getVendorSpecific();
        if (!vendorSpecific.isEmpty()) {
            final MetadataElement vendorElement = new MetadataElement("VendorSpecific");
            for (final Map.Entry<String, String> entry : vendorSpecific.entrySet()) {
                FlexReaderUtils.addStringAttribute(vendorElement, entry.getKey(), entry.getValue());
            }
            metadataRoot.addElement(vendorElement);
        }
    }

    private void addMetadataVariables(Product product) {
        if (metadataMap.isEmpty()) {
            return;
        }
        final MetadataElement metadataRoot = product.getMetadataRoot();
        final MetadataElement baseElement = new MetadataElement(NETCDF_BASE_METADATA_ELEMENT);
        for (final FlexVariableDescriptor descriptor : metadataMap.values()) {
            final FlexMetadataElementLazy lazyElement = new FlexMetadataElementLazy(descriptor.getName(), this);
            baseElement.addElement(lazyElement);
        }
        metadataRoot.addElement(baseElement);
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
        return FlexReaderUtils.extractMetadata(ncVariable);
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


    private static final Color[] MASK_COLORS = {
            new Color(0, 100, 0), new Color(0, 0, 200), new Color(200, 0, 0),
            new Color(200, 200, 0), new Color(0, 200, 200), new Color(200, 0, 200),
            new Color(100, 100, 0), new Color(0, 100, 100), new Color(100, 0, 100),
            new Color(128, 128, 128)
    };
}
