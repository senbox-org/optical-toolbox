package eu.esa.opt.dataio.s3;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.dddb.*;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.manifest.ManifestUtil;
import eu.esa.opt.dataio.s3.manifest.XfduManifest;
import eu.esa.opt.dataio.s3.olci.InstrumentBand;
import eu.esa.opt.dataio.s3.olci.ReaderContext;
import eu.esa.opt.dataio.s3.util.*;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.esa.snap.dataio.netcdf.util.ReaderUtils;
import org.esa.snap.engine_utilities.util.ZipUtils;
import org.esa.snap.runtime.Config;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
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

import static eu.esa.opt.dataio.s3.dddb.VariableType.*;
import static eu.esa.opt.dataio.s3.util.S3NetcdfReader.extractMetadata;
import static eu.esa.opt.dataio.s3.util.S3Util.*;

public class Sentinel3DddbReader extends AbstractProductReader implements MetadataProvider, ReaderContext {

    private final DDDB dddb;
    private final Map<String, VariableDescriptor> tiepointMap;
    private final Map<String, VariableDescriptor> metadataMap;
    private final Map<String, VariableDescriptor> variablesMap;
    private final Map<String, VariableDescriptor> specialsMap;
    private final Map<String, Variable> ncVariablesMap;
    private final Map<String, NetcdfFile> filesMap;
    private final Map<String, Array> dataMap;
    private final Map<String, Float> nameToWavelengthMap;
    private final Map<String, Float> nameToBandwidthMap;
    private final Map<String, Integer> nameToIndexMap;
    private VirtualDir virtualDir;
    private Manifest manifest;
    private ColorProvider colorProvider;
    private SensorContext sensorContext;

    protected Sentinel3DddbReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        virtualDir = null;
        manifest = null;
        colorProvider = null;
        dddb = DDDB.getInstance();
        sensorContext = null;
        // the treemap sorts the entries alphanumerically - as these data should be displayed in the SNAP product tree tb 2025-02-04
        variablesMap = new TreeMap<>();
        tiepointMap = new TreeMap<>();
        specialsMap = new TreeMap<>();
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

    // package access for testing only tb 2025-02-11
    static void ensureWidthAndHeight(VariableDescriptor variableDescriptor, Manifest manifest) {
        int width = variableDescriptor.getWidth();
        int height = variableDescriptor.getHeight();

        if (width < 0 || height < 0) {
            width = manifest.getXPathInt(variableDescriptor.getWidthXPath());
            variableDescriptor.setWidth(width);
            height = manifest.getXPathInt(variableDescriptor.getHeightXPath());
            variableDescriptor.setHeight(height);
        }

        if (variableDescriptor.getVariableType() == TIE_POINT) {
            int tpSubsamplingX = variableDescriptor.getTpSubsamplingX();
            int tpSubsamplingY = variableDescriptor.getTpSubsamplingY();
            if (tpSubsamplingX < 0 || tpSubsamplingY < 0) {
                final int subsamplingX = manifest.getXPathInt(variableDescriptor.getTpXSubsamplingXPath());
                final int subsamplingY = manifest.getXPathInt(variableDescriptor.getTpYSubsamplingXPath());
                final int tpWidth = (int) Math.ceil((double) width / subsamplingX);
                final int tpHeight = (int) Math.ceil((double) height / subsamplingY);

                variableDescriptor.setTpSubsamplingX(subsamplingX);
                variableDescriptor.setTpSubsamplingY(subsamplingY);
                variableDescriptor.setWidth(tpWidth);
                variableDescriptor.setHeight(tpHeight);
            }
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
    public static void extractSubset(RasterExtract rasterExtract, ProductData destBuffer, Array rawDataArray, double scaleFactor, double offset, boolean rawData) throws IOException {
        final int[] sliceOffset = new int[]{rasterExtract.getYOffset(), rasterExtract.getXOffset()};
        //final int stepY = Math.max(rasterExtract.getStepY() - 1, 1);
        //final int stepX = Math.max(rasterExtract.getStepX()-1 , 1);
        final int stepY = Math.max(rasterExtract.getStepY(), 1);
        final int stepX = Math.max(rasterExtract.getStepX(), 1);
        final int dimY = (int) Math.ceil(rasterExtract.getHeight() / (float) stepY);
        final int dimX = (int) Math.ceil(rasterExtract.getWidth() / (float) stepX);
        final int[] sliceDimensions = new int[]{dimY, dimX};
        final int[] stride = new int[]{stepY, stepX};

        try {
            Array sliceData = rawDataArray.section(sliceOffset, sliceDimensions, stride).copy().reduce();
            if (!rawData) {
                sliceData = ReaderUtils.scaleArray(sliceData, scaleFactor, offset);
            }
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

    // package access for testing only tb 2025-02-12
    public static String getLayerName(String variableFullName, int i) {
        return variableFullName + "_band_" + i;
    }

    // package access for testing only tb 2025-02-12
    static boolean isLayerName(String layerName) {
        return layerName.contains("_band_");
    }

    public static int getLayerIndexFromLayerName(String layerName) {
        String band = "_band_";
        final int index = layerName.indexOf(band);
        if (index < 0) {
            return -1;
        }

        final String numberString = layerName.substring(index + band.length());
        return Integer.parseInt(numberString);
    }

    public static String getVariableNameFromLayerName(String layerName) {
        int index = layerName.indexOf("_band_");
        if (index >= 0) {
            return layerName.substring(0, index);
        } else {
            return layerName;
        }
    }

    // package access for testing only tb 2025-02-12
    static boolean isPressureLevelName(String layerName) {
        return layerName.contains("_pressure_level_");
    }

    // package access for testing only tb 2025-02-12
    static String getVariableNameFromPressureLevelName(String layerName) {
        int index = layerName.indexOf("_pressure_level_");
        if (index >= 0) {
            return layerName.substring(0, index);
        } else {
            return layerName;
        }
    }

    // package access for testing only tb 2025-02-12
    static int getLayerIndexFromPressureLevelName(String layerName) {
        String band = "_pressure_level_";
        final int index = layerName.indexOf(band);
        if (index < 0) {
            return -1;
        }

        final String numberString = layerName.substring(index + band.length());
        return Integer.parseInt(numberString);
    }

    // package access for testing only tb 2025-02-12
    static int getLayerIndexFromTiePointName(String layerName, VariableDescriptor variableDescriptor) {
        final String token = variableDescriptor.getDepthPrefixToken();
        if (StringUtils.isNotNullAndNotEmpty(token)) {
            final int index = layerName.indexOf(token);
            if (index < 0) {
                return -1;
            }
            final String numberString = layerName.substring(index + token.length());
            return Integer.parseInt(numberString);
        }

        return -1;
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        initalizeInput();

        manifest = readManifest();

        final String productType = manifest.getProductType();
        sensorContext = SensorContextFactory.get(productType);
        sensorContext.setReaderContext(this);

        final MetadataElement bandDescriptionsElement = sensorContext.getBandDescriptionsElement(manifest);
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

        // create a naked product
        final String productName = manifest.getProductName();
        final String baselineCollection = manifest.getBaselineCollection();
        final ProductDescriptor productDescriptor = dddb.getProductDescriptor(productType, baselineCollection);
        ensureWidthAndHeight(productDescriptor, manifest);

        final Product product = new Product(productName, productType, productDescriptor.getWidth(), productDescriptor.getHeight(), this);

        // add other properties from manifest and descriptor
        product.setDescription(manifest.getDescription());
        product.setFileLocation(new File(getInput().toString()));
        product.setAutoGrouping(productDescriptor.getBandGroupingPattern());
        product.setStartTime(manifest.getStartTime());
        product.setEndTime(manifest.getStopTime());

        initializeDDDBDescriptors(manifest, productDescriptor);
        addVariables(product);
        addTiePointGrids(product, manifest);
        addSpecialBands(product);
        setMasks(product);

        for (FlagMask flagMask : productDescriptor.getFlagMasks()) {
            final Color maskColor = colorProvider.getMaskColor(flagMask.getName());
            product.addMask(flagMask.getName(), flagMask.getExpression(), flagMask.getDescription(), maskColor, 0.5);
        }


        // metadata
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(manifest.getMetadata());
        for (VariableDescriptor metaDescriptor : metadataMap.values()) {
            final MetadataElementLazy metadataElementLazy = new MetadataElementLazy(metaDescriptor.getName(), this);
            metadataRoot.addElement(metadataElementLazy);
        }

        return product;
    }

    @Override
    public boolean isSubsetReadingFullySupported() {
        return true;
    }

    @Override
    public GeoCoding readGeoCoding(Product product) throws IOException {
        if (Config.instance("opttbx").load().preferences().getBoolean(sensorContext.getUsePixelGeoCodingKey(), true)) {
            return createPixelGeoCoding(product);
        } else {
            return createTiePointGeoCoding(product);
        }
    }

    private ComponentGeoCoding createPixelGeoCoding(Product product) throws IOException {
        final GeoLocationNames geoLocationNames = sensorContext.getGeoLocationNames();
        final Band lonBand = product.getBand(geoLocationNames.getLongitudeName());
        final Band latBand = product.getBand(geoLocationNames.getLatitudeName());
        if (lonBand == null || latBand == null) {
            return null;
        }

        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();
        final RasterExtract rasterExtract = new RasterExtract(0, 0, width, height, 1, 1);

        final ProductData productDataLon = ProductData.createInstance(new double[width * height]);
        final ProductData productDataLat = ProductData.createInstance(new double[width * height]);

        final VariableDescriptor lonDescriptor = variablesMap.get(geoLocationNames.getLongitudeName());
        readData(rasterExtract, productDataLon, lonDescriptor, geoLocationNames.getLongitudeName(), false);

        final VariableDescriptor latDescriptor = variablesMap.get(geoLocationNames.getLatitudeName());
        readData(rasterExtract, productDataLat, latDescriptor, geoLocationNames.getLatitudeName(), false);

        final double resolutionInKm = sensorContext.getResolutionInKm(product.getProductType());
        final GeoRaster geoRaster = new GeoRaster((double[]) productDataLon.getElems(), (double[]) productDataLat.getElems(),
                geoLocationNames.getLongitudeName(), geoLocationNames.getLatitudeName(),
                lonBand.getRasterWidth(), lonBand.getRasterHeight(), resolutionInKm);

        final String[] codingKeys = getForwardAndInverseKeys_pixelCoding(sensorContext.getInversePixelGeoCodingKey());
        final ForwardCoding forward = ComponentFactory.getForward(codingKeys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(codingKeys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        return geoCoding;
    }

    private ComponentGeoCoding createTiePointGeoCoding(Product product) throws IOException {
        final GeoLocationNames geoLocationNames = sensorContext.getGeoLocationNames();
        TiePointGrid lonGrid = product.getTiePointGrid(geoLocationNames.getTpLongitudeName());
        TiePointGrid latGrid = product.getTiePointGrid(geoLocationNames.getTpLatitudeName());
        if (latGrid == null || lonGrid == null) {
            return null;
        }

        final VariableDescriptor lonDescriptor = tiepointMap.get(geoLocationNames.getTpLongitudeName());
        ensureWidthAndHeight(lonDescriptor, manifest);
        final VariableDescriptor latDescriptor = tiepointMap.get(geoLocationNames.getTpLatitudeName());
        ensureWidthAndHeight(latDescriptor, manifest);

        final int width = lonDescriptor.getWidth();
        final int height = lonDescriptor.getHeight();
        final int bufferSize = width * height;
        final ProductData productDataLon = ProductData.createInstance(new double[bufferSize]);
        final ProductData productDataLat = ProductData.createInstance(new double[bufferSize]);

        final RasterExtract rasterExtract = new RasterExtract(0, 0, width, height, 1, 1);
        readData(rasterExtract, productDataLon, lonDescriptor, geoLocationNames.getTpLongitudeName(), true);
        readData(rasterExtract, productDataLat, latDescriptor, geoLocationNames.getTpLatitudeName(), true);

        final double resolutionInKm = sensorContext.getResolutionInKm(product.getProductType());
        final GeoRaster geoRaster = new GeoRaster((double[]) productDataLon.getElems(), (double[]) productDataLat.getElems(),
                geoLocationNames.getTpLongitudeName(), geoLocationNames.getTpLatitudeName(),
                lonGrid.getGridWidth(), lonGrid.getGridHeight(),
                product.getSceneRasterWidth(), product.getSceneRasterHeight(), resolutionInKm,
                lonGrid.getOffsetX(), lonGrid.getOffsetY(),
                lonGrid.getSubSamplingX(), lonGrid.getSubSamplingY());

        final String[] codingKeys = getForwardAndInverseKeys_tiePointCoding();
        final ForwardCoding forward = ComponentFactory.getForward(codingKeys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(codingKeys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        return geoCoding;
    }

    private void addVariables(Product product) throws IOException {
        for (VariableDescriptor descriptor : variablesMap.values()) {
            final int dataType = ProductData.getType(descriptor.getDataType());

            if (dataType == ProductData.TYPE_INT64 || dataType == ProductData.TYPE_UINT64) {
                addSplittedBand(product, descriptor);
            } else {
                addBand(product, descriptor, dataType);
            }
        }
    }

    private void addSplittedBand(Product product, VariableDescriptor descriptor) throws IOException {
        final String bandname = descriptor.getName();
        final boolean isMsb = bandname.contains("_msb");
        final Band band = new BandUsingReaderDirectly(bandname, ProductData.TYPE_UINT32, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        product.addBand(band);
        setSpectralProperties(bandname, band);

        Variable netCDFVariable = getNetCDFVariable(descriptor, descriptor.getNcVarName());
        S3Util.addSampleCodings(product, band, netCDFVariable, isMsb);
        setScalefactorAndOffset(band, netCDFVariable);
        band.setValidPixelExpression(descriptor.getValidExpression());
        sensorContext.applyCalibration(band);
        sensorContext.addDescriptionAndUnit(band, descriptor);
        S3Util.addFillValue(band, netCDFVariable);
    }

    private void addBand(Product product, VariableDescriptor descriptor, int dataType) throws IOException {
        final String bandname = descriptor.getName();

        final Band band = new BandUsingReaderDirectly(bandname, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        product.addBand(band);

        setSpectralProperties(bandname, band);

        final Variable netCDFVariable = getNetCDFVariable(descriptor, bandname);
        S3Util.addSampleCodings(product, band, netCDFVariable, false);
        setScalefactorAndOffset(band, netCDFVariable);
        band.setValidPixelExpression(descriptor.getValidExpression());
        sensorContext.applyCalibration(band);
        sensorContext.addDescriptionAndUnit(band, descriptor);
        S3Util.addFillValue(band, netCDFVariable);
    }

    private static void setScalefactorAndOffset(Band band, Variable netCDFVariable) {
        band.setScalingFactor(getScalingFactor(netCDFVariable));
        band.setScalingOffset(S3Util.getAddOffset(netCDFVariable));
    }

    private void setSpectralProperties(String bandname, Band band) {
        final String bandKey = sensorContext.bandNameToKey(bandname);
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

    private void addTiePointGrids(Product product, Manifest manifest) {
        for (VariableDescriptor descriptor : tiepointMap.values()) {
            ensureWidthAndHeight(descriptor, manifest);
            final String tiePointName = descriptor.getName();
            final int subsamplingX = manifest.getXPathInt(descriptor.getTpXSubsamplingXPath());
            final int subsamplingY = manifest.getXPathInt(descriptor.getTpYSubsamplingXPath());

            final int tpRasterWidth = (int) Math.ceil((double) product.getSceneRasterWidth() / subsamplingX);
            final int tpRasterHeight = (int) Math.ceil((double) product.getSceneRasterHeight() / subsamplingY);

            int depth = descriptor.getDepth();
            if (depth != -1) {
                // @todo 1 tb/tb this is the simplest make-it-work-hack. Refactor and merge with existing
                // functionality - add tests! 2025-02-14
                final String layerPrefix = tiePointName + descriptor.getDepthPrefixToken();
                for (int layer = 1; layer <= depth; layer++) {
                    final String layerName = layerPrefix + layer;
                    final LayeredTiePointGrid tiePointGrid = new LayeredTiePointGrid(layerName, tpRasterWidth, tpRasterHeight, 0.0, 0.0, subsamplingX, subsamplingY);
                    tiePointGrid.setDescription(descriptor.getDescription());
                    tiePointGrid.setUnit(descriptor.getUnits());
                    tiePointGrid.setVariableName(tiePointName);
                    product.addTiePointGrid(tiePointGrid);
                }
            } else {
                final TiePointGrid tiePointGrid = new TiePointGrid(tiePointName, tpRasterWidth, tpRasterHeight, 0.0, 0.0, subsamplingX, subsamplingY);
                tiePointGrid.setDescription(descriptor.getDescription());
                tiePointGrid.setUnit(descriptor.getUnits());
                product.addTiePointGrid(tiePointGrid);
            }
        }
    }

    private void addSpecialBands(Product product) throws IOException {
        // @todo 2 tb/tb check if and how to synchronise 2025-02-12
        for (VariableDescriptor variableDescriptor : specialsMap.values()) {
            Variable specialVariable = getNetCDFVariable(variableDescriptor, variableDescriptor.getName());
            if (specialVariable == null) {
                continue;
            }
            final String variableFullName = specialVariable.getFullName();
            final int dimensionIndex = specialVariable.findDimensionIndex("bands");
            if (dimensionIndex != -1) {
                final int numBands = specialVariable.getDimension(dimensionIndex).getLength();
                for (int i = 1; i <= numBands; i++) {
                    final String layerName = getLayerName(variableFullName, i);
                    addVariableAsBand(product, specialVariable, layerName, true);
                }
            } else {
                addVariableAsBand(product, specialVariable, variableFullName, false);
            }
        }
    }

    protected void addVariableAsBand(Product product, Variable variable, String variableName, boolean synthetic) {
        final int type = S3Util.getRasterDataType(variable);

        final InstrumentBand band = new InstrumentBand(variableName, type, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        band.setDescription(variable.getDescription());
        band.setUnit(variable.getUnitsString());
        setScalefactorAndOffset(band, variable);
        band.setSpectralWavelength(S3Util.getSpectralWavelength(variable));
        band.setSpectralBandwidth(S3Util.getSpectralBandwidth(variable));
        band.setSynthetic(synthetic);
        S3Util.addSampleCodings(product, band, variable, false);
        S3Util.addFillValue(band, variable);

        band.setReaderContext(this);

        product.addBand(band);
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
                final VariableType variableType = descriptor.getVariableType();
                if (variableType == VARIABLE || variableType == FLAG) {
                    variablesMap.put(descriptorKey, descriptor);
                } else if (variableType == TIE_POINT) {
                    tiepointMap.put(descriptorKey, descriptor);
                } else if (variableType == METADATA) {
                    metadataMap.put(descriptorKey, descriptor);
                } else if (variableType == SPECIAL) {
                    specialsMap.put(descriptorKey, descriptor);
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
        final VariableDescriptor descriptor;
        final RasterExtract rasterExtract;

        final String destBandName = destBand.getName();
        if (destBand instanceof InstrumentBand instrumentBand) {
            final String variableName = getVariableNameFromLayerName(destBandName);
            descriptor = specialsMap.get(variableName);
            instrumentBand.readRasterDataFully();
        } else {
            descriptor = variablesMap.get(destBandName);
        }

        rasterExtract = new RasterExtract(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY);
        readData(rasterExtract, destBuffer, descriptor, destBandName, true);
    }

    @Override
    public void readTiePointGridRasterData(TiePointGrid tpg, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {
        final String tpGridName = tpg.getName();

        final ProductData gridData = tpg.getData();
        if (gridData == null) {

            final String ncVariableName;
            if (tpg instanceof LayeredTiePointGrid) {
                ncVariableName = ((LayeredTiePointGrid) tpg).getVariableName();
            } else {
                ncVariableName = tpGridName;
            }

            final VariableDescriptor descriptor = tiepointMap.get(ncVariableName);
            final Variable netCDFVariable = getNetCDFVariable(descriptor, ncVariableName);

            Array rawDataArray = getRawData(tpGridName, netCDFVariable);
            final int layer = getLayerIndexFromTiePointName(tpGridName, descriptor);
            if (layer >= 0) {
                final int[] sliceOffset = new int[]{0, 0, layer - 1};  // shift to zero based z coord. tb 2025-03-26
                final int[] sliceDimensions = new int[]{tpg.getGridHeight(), tpg.getGridWidth(), 1};
                final int[] stride = new int[]{1, 1, 1};
                try {
                    rawDataArray = rawDataArray.section(sliceOffset, sliceDimensions, stride);
                } catch (InvalidRangeException e) {
                    throw new IOException(e);
                }
            }

            final Array scaledData = ReaderUtils.scaleArray(rawDataArray, netCDFVariable);
            final ProductData tiePointData = ProductData.createInstance((float[]) scaledData.get1DJavaArray(DataType.FLOAT));
            tpg.setData(tiePointData);
        }

        System.arraycopy(tpg.getGridData().getElems(), 0, destBuffer.getElems(), 0, destWidth * destHeight);
    }

    private synchronized void readData(RasterExtract rasterExtract, ProductData destBuffer, VariableDescriptor descriptor, String name, boolean rawData) throws IOException {
        final Variable netCDFVariable = getNetCDFVariable(descriptor, name);
        final Array rawDataArray = getRawData(name, netCDFVariable);

        final double scalingFactor = getScalingFactor(netCDFVariable);
        double offset = getAddOffset(netCDFVariable);
        extractSubset(rasterExtract, destBuffer, rawDataArray, scalingFactor, offset, rawData);
    }

    private Variable getNetCDFVariable(VariableDescriptor descriptor, String name) throws IOException {
        final NetcdfFile netcdfFile = getNetcdfFile(descriptor.getFileName());

        String variableName = descriptor.getName();
        final String ncVarName = descriptor.getNcVarName();
        if (StringUtils.isNotNullAndNotEmpty(ncVarName)) {
            variableName = ncVarName;
        }

        Variable ncVar;

        synchronized (ncVariablesMap) {
            Variable variable;

            variable = ncVariablesMap.get(name);
            if (variable == null) {
                variable = netcdfFile.findVariable(name);
                if (variable == null) {
                    variable = netcdfFile.findVariable(variableName);
                    if (variable == null) {
                        throw new IOException("requested variable not found: " + name + " " + netcdfFile.getLocation());
                    }
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

                final ArrayConverter converter = getArrayConverter(name);
                fullDataArray = converter.convert(fullDataArray);

                dataMap.put(name, fullDataArray);
            }
        }
        return fullDataArray;
    }

    private static ArrayConverter getArrayConverter(String name) {
        ArrayConverter converter;
        converter = ArrayConverter.IDENTITY;

        if (name.endsWith("_msb")) {
            converter = ArrayConverter.MSB;
        }

        if (name.endsWith("_lsb")) {
            converter = ArrayConverter.LSB;
        }
        return converter;
    }

    private NetcdfFile getNetcdfFile(String fileName) throws IOException {
        NetcdfFile netcdfFile;
        synchronized (filesMap) {
            netcdfFile = filesMap.get(fileName);
            if (netcdfFile == null) {
                final File file = getFileFromVirtualDir(fileName, virtualDir);
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
        specialsMap.clear();

        for (final NetcdfFile ncFile : filesMap.values()) {
            ncFile.close();
        }
        filesMap.clear();
        dataMap.clear();

        manifest = null;
        sensorContext = null;
        colorProvider = null;

        super.close();
    }

    private Path getInputPath() {
        return Paths.get(getInput().toString());
    }


    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // MetadataProvider
    @Override
    public MetadataElement readElement(String name) throws IOException {
        final VariableDescriptor descriptor = metadataMap.get(name);
        if (descriptor == null) {
            return null;
        }
        final Variable netCDFVariable = getNetCDFVariable(descriptor, name);
        if (netCDFVariable == null) {
            return null;
        }

        return extractMetadata(netCDFVariable);
    }

    /// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setMasks(Product targetProduct) {
        final Band[] bands = targetProduct.getBands();

        ColorProvider colorProvider = getColorProvider();
        colorProvider.reset();

        for (Band band : bands) {
            final SampleCoding sampleCoding = band.getSampleCoding();
            if (sampleCoding != null) {
                final String bandName = band.getName();
                if (bandName.endsWith("_index")) {
                    continue;
                }
                final boolean isFlagBand = band.isFlagBand();
                for (int i = 0; i < sampleCoding.getNumAttributes(); i++) {
                    final String sampleName = sampleCoding.getSampleName(i);
                    final int sampleValue = sampleCoding.getSampleValue(i);
                    if (!"spare".equals(sampleName)) {
                        final String expression;
                        if (isFlagBand) {
                            expression = bandName + "." + sampleName;
                        } else {
                            expression = bandName + " == " + sampleValue;
                        }
                        final String maskName = bandName + "_" + sampleName;

                        final Color maskColor = colorProvider.getMaskColor(sampleName);
                        targetProduct.addMask(maskName, expression, expression, maskColor, 0.5);
                    }
                }
            }
        }
    }

    private ColorProvider getColorProvider() {
        if (colorProvider == null) {
            colorProvider = new ColorProvider();
        }

        return colorProvider;
    }

    @Override
    public Array readData(String name, Variable netCDFVariable) throws IOException {
        return getRawData(name, netCDFVariable);
    }

    @Override
    public Array readData(String name) throws IOException {
        VariableDescriptor variableDescriptor = variablesMap.get(name);
        if (variableDescriptor == null) {
            variableDescriptor = specialsMap.get(name);
        }
        final Variable netCDFVariable = getNetCDFVariable(variableDescriptor, name);

        return getRawData(name, netCDFVariable);
    }

    @Override
    public boolean hasData(String name) {
        return dataMap.containsKey(name);
    }

    @Override
    public void ingestToCache(String name, Array data) {
        dataMap.put(name, data);
    }
}
