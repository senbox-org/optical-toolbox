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
import eu.esa.opt.dataio.s3.olci.OlciProductFactory;
import eu.esa.opt.dataio.s3.util.ColorProvider;
import eu.esa.opt.dataio.s3.util.LayeredTiePointGrid;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.dataio.netcdf.util.Constants;
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
import ucar.nc2.Attribute;
import ucar.nc2.AttributeContainer;
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
import java.util.prefs.Preferences;

import static eu.esa.opt.dataio.s3.dddb.VariableType.*;
import static eu.esa.opt.dataio.s3.olci.OlciProductFactory.isLogScaledUnit;
import static eu.esa.opt.dataio.s3.olci.OlciProductFactory.stripLogFromDescription;
import static eu.esa.opt.dataio.s3.util.S3NetcdfReader.extractMetadata;
import static eu.esa.opt.dataio.s3.util.S3Util.*;

public class Sentinel3Level1Reader extends AbstractProductReader implements MetadataProvider {

    // @todo 1 tb/tb move to OLCI specific class 2025-02-12
    public static final String LON_VAR_NAME = "longitude";
    public static final String LAT_VAR_NAME = "latitude";
    public static final String TP_LON_VAR_NAME = "TP_longitude";
    public static final String TP_LAT_VAR_NAME = "TP_latitude";

    // @todo 1 tb/tb move to general utilities class 2025-02-12
    private static final String flag_values = "flag_values";
    private static final String flag_masks = "flag_masks";
    private static final String flag_meanings = "flag_meanings";
    private static final String fillValue = "_FillValue";

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

    protected Sentinel3Level1Reader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        virtualDir = null;
        manifest = null;
        colorProvider = null;
        dddb = DDDB.getInstance();
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
    private static void extractSubset(RasterExtract rasterExtract, ProductData destBuffer, Array rawDataArray, Variable netCDFVariable, boolean rawData) throws IOException {
        final int[] sliceOffset;
        final int[] sliceDimensions;
        final int[] stride;

        final int layerIdx = rasterExtract.getLayerIdx();
        if (layerIdx >= 0) {
            sliceOffset = new int[]{rasterExtract.getYOffset(), rasterExtract.getXOffset(), layerIdx};
            sliceDimensions = new int[]{rasterExtract.getHeight(), rasterExtract.getWidth(), 1};
            stride = new int[]{rasterExtract.getStepY(), rasterExtract.getStepX(), 1};
        } else {
            sliceOffset = new int[]{rasterExtract.getYOffset(), rasterExtract.getXOffset()};
            sliceDimensions = new int[]{rasterExtract.getHeight(), rasterExtract.getWidth()};
            stride = new int[]{rasterExtract.getStepY(), rasterExtract.getStepX()};
        }

        try {
            Array sliceData = rawDataArray.section(sliceOffset, sliceDimensions, stride);
            if (!rawData) {
                sliceData = ReaderUtils.scaleArray(sliceData, netCDFVariable);
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

    static String bandNameToKey(String bandName) {
        // @todo 1 tb/tb this is OLCI specific - extract sensor specific class 2025-01-06
        return bandName.substring(0, 4);
    }

    // package access for testing only tb 2025-02-12
    static String getLayerName(String variableFullName, int i) {
        return variableFullName + "_band_" + i;
    }

    // package access for testing only tb 2025-02-12
    static boolean isLayerName(String layerName) {
        return layerName.contains("_band_");
    }

    // package access for testing only tb 2025-02-12
    static int getLayerIndexFromLayerName(String layerName) {
        String band = "_band_";
        final int index = layerName.indexOf(band);
        if (index < 0) {
            return -1;
        }

        final String numberString = layerName.substring(index + band.length());
        return Integer.parseInt(numberString);
    }

    // package access for testing only tb 2025-02-12
    static String getVariableNameFromLayerName(String layerName) {
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

    private static void addSamples(SampleCoding sampleCoding, Attribute sampleMeanings, Attribute sampleValues,
                                   Attribute sampleMasks, boolean msb) {
        final String[] meanings = getSampleMeanings(sampleMeanings);
        String[] uniqueNames = StringUtils.makeStringsUnique(meanings);
        final int sampleCount = Math.min(uniqueNames.length, sampleMasks.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = replaceNonWordCharacters(uniqueNames[i]);
            switch (sampleMasks.getDataType()) {
                case BYTE:
                case UBYTE:
                    int[] byteValues = {
                            DataType.unsignedByteToShort(sampleMasks.getNumericValue(i).byteValue()),
                            DataType.unsignedByteToShort(sampleValues.getNumericValue(i).byteValue())
                    };
                    if (byteValues[0] == byteValues[1]) {
                        sampleCoding.addSample(sampleName, byteValues[0], null);
                    } else {
                        sampleCoding.addSamples(sampleName, byteValues, null);
                    }
                    break;
                case SHORT:
                case USHORT:
                    int[] shortValues = {
                            DataType.unsignedShortToInt(sampleMasks.getNumericValue(i).shortValue()),
                            DataType.unsignedShortToInt(sampleValues.getNumericValue(i).shortValue())
                    };
                    if (shortValues[0] == shortValues[1]) {
                        sampleCoding.addSample(sampleName, shortValues[0], null);
                    } else {
                        sampleCoding.addSamples(sampleName, shortValues, null);
                    }
                    break;
                case INT:
                case UINT:
                    int[] intValues = {
                            sampleMasks.getNumericValue(i).intValue(),
                            sampleValues.getNumericValue(i).intValue()
                    };
                    if (intValues[0] == intValues[1]) {
                        sampleCoding.addSample(sampleName, intValues[0], null);
                    } else {
                        sampleCoding.addSamples(sampleName, intValues, null);
                    }
                    break;
                case LONG:
                case ULONG:
                    long[] longValues = {
                            sampleMasks.getNumericValue(i).longValue(),
                            sampleValues.getNumericValue(i).longValue()
                    };
                    if (msb) {
                        int[] intLongValues =
                                {(int) (longValues[0] >>> 32), (int) (longValues[1] >>> 32)};
                        if (longValues[0] > 0) {
                            if (intLongValues[0] == intLongValues[1]) {
                                sampleCoding.addSample(sampleName, intLongValues[0], null);
                            } else {
                                sampleCoding.addSamples(sampleName, intLongValues, null);
                            }
                        }
                    } else {
                        int[] intLongValues =
                                {(int) (longValues[0] & 0x00000000FFFFFFFFL), (int) (longValues[1] & 0x00000000FFFFFFFFL)};
                        if (intLongValues[0] > 0 || longValues[0] == 0L) {
                            if (intLongValues[0] == intLongValues[1]) {
                                sampleCoding.addSample(sampleName, intLongValues[0], null);
                            } else {
                                sampleCoding.addSamples(sampleName, intLongValues, null);
                            }
                        }
                    }
                    break;
            }
        }
    }

    private static void addSamples(SampleCoding sampleCoding, Attribute sampleMeanings, Attribute sampleValues,
                                   boolean msb) {
        final String[] meanings = getSampleMeanings(sampleMeanings);
        String[] uniqueNames = StringUtils.makeStringsUnique(meanings);
        final int sampleCount = Math.min(uniqueNames.length, sampleValues.getLength());
        for (int i = 0; i < sampleCount; i++) {
            final String sampleName = replaceNonWordCharacters(uniqueNames[i]);
            switch (sampleValues.getDataType()) {
                case BYTE:
                case UBYTE:
                    sampleCoding.addSample(sampleName,
                            DataType.unsignedByteToShort(sampleValues.getNumericValue(i).byteValue()),
                            null);
                    break;
                case SHORT:
                case USHORT:
                    sampleCoding.addSample(sampleName,
                            DataType.unsignedShortToInt(sampleValues.getNumericValue(i).shortValue()), null);
                    break;
                case INT:
                case UINT:
                    sampleCoding.addSample(sampleName, sampleValues.getNumericValue(i).intValue(), null);
                    break;
                case LONG:
                case ULONG:
                    final long longValue = sampleValues.getNumericValue(i).longValue();
                    if (msb) {
                        long shiftedValue = longValue >>> 32;
                        if (shiftedValue > 0) {
                            sampleCoding.addSample(sampleName, (int) shiftedValue, null);
                        }
                    } else {
                        long shiftedValue = longValue & 0x00000000FFFFFFFFL;
                        if (shiftedValue > 0 || longValue == 0L) {
                            sampleCoding.addSample(sampleName, (int) shiftedValue, null);
                        }
                    }
                    break;
            }
        }
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    static String replaceNonWordCharacters(String flagName) {
        return flagName.replaceAll("\\W+", "_");
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private static String[] getSampleMeanings(Attribute sampleMeanings) {
        final int sampleMeaningsCount = sampleMeanings.getLength();
        if (sampleMeaningsCount == 0) {
            return new String[0];
        }
        if (sampleMeaningsCount > 1) {
            // handle a common misunderstanding of CF conventions, where flag meanings are stored as array of strings
            final String[] strings = new String[sampleMeaningsCount];
            for (int i = 0; i < strings.length; i++) {
                strings[i] = sampleMeanings.getStringValue(i);
            }
            return strings;
        }
        return sampleMeanings.getStringValue().split(" ");
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private static float getSpectralWavelength(Variable variable) {
        Attribute attribute = variable.findAttribute("wavelength");
        if (attribute != null) {
            return getAttributeValue(attribute).floatValue();
        }
        return 0f;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private static float getSpectralBandwidth(Variable variable) {
        Attribute attribute = variable.findAttribute("bandwidth");
        if (attribute != null) {
            return getAttributeValue(attribute).floatValue();
        }
        return 0f;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    protected static double getScalingFactor(Variable variable) {
        Attribute attribute = variable.findAttribute(Constants.SCALE_FACTOR_ATT_NAME);
        if (attribute == null) {
            attribute = variable.findAttribute(Constants.SLOPE_ATT_NAME);
        }
        if (attribute == null) {
            attribute = variable.findAttribute("scaling_factor");
        }
        if (attribute != null) {
            return getAttributeValue(attribute).doubleValue();
        }
        return 1.0;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    protected static double getAddOffset(Variable variable) {
        Attribute attribute = variable.findAttribute(Constants.ADD_OFFSET_ATT_NAME);
        if (attribute == null) {
            attribute = variable.findAttribute(Constants.INTERCEPT_ATT_NAME);
        }
        if (attribute != null) {
            return getAttributeValue(attribute).doubleValue();
        }
        return 0.0;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private static Number getAttributeValue(Attribute attribute) {
        if (attribute.isString()) {
            String stringValue = attribute.getStringValue();
            if (stringValue.endsWith("b")) {
                // Special management for bytes; Can occur in e.g. ASCAT files from EUMETSAT
                return Byte.parseByte(stringValue.substring(0, stringValue.length() - 1));
            } else {
                return Double.parseDouble(stringValue);
            }
        } else {
            return attribute.getNumericValue();
        }
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private static int getRasterDataType(Variable variable) {
        int rasterDataType = DataTypeUtils.getRasterDataType(variable);
        if (rasterDataType == -1 && variable.getDataType() == DataType.LONG) {
            rasterDataType = variable.getDataType().isUnsigned() ? ProductData.TYPE_UINT32 : ProductData.TYPE_INT32;
        }
        return rasterDataType;
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        initalizeInput();

        manifest = readManifest();
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

        // create a naked product
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
        product.setStartTime(manifest.getStartTime());
        product.setEndTime(manifest.getStopTime());

        initializeDDDBDescriptors(manifest, productDescriptor);
        addVariables(product);
        addTiePointGrids(product, manifest);
        addSpecialBands(product, manifest);
        setMasks(product);

        // metadata
        final MetadataElement metadataRoot = product.getMetadataRoot();
        metadataRoot.addElement(metadata);
        for (VariableDescriptor metaDescriptor : metadataMap.values()) {
            final MetadataElementLazy metadataElementLazy = new MetadataElementLazy(metaDescriptor.getName(), this);
            metadataRoot.addElement(metadataElementLazy);
        }

        return product;
    }

    @Override
    public GeoCoding readGeoCoding(Product product) throws IOException {

        if (Config.instance("opttbx").load().preferences().getBoolean(OLCI_USE_PIXELGEOCODING, true)) {
            // @todo 1 tb/tb distinguish pixel or tiepoint geocoding, load accordingly 2025-02-07
            return createPixelGeoCoding(product);
        } else {
            return createTiePointGeoCoding(product);
        }
    }

    private ComponentGeoCoding createPixelGeoCoding(Product product) throws IOException {
        final Band lonBand = product.getBand(LON_VAR_NAME);
        final Band latBand = product.getBand(LAT_VAR_NAME);
        if (lonBand == null || latBand == null) {
            return null;
        }

        final int width = product.getSceneRasterWidth();
        final int height = product.getSceneRasterHeight();
        final RasterExtract rasterExtract = new RasterExtract(0, 0, width, height, 1, 1);

        final ProductData productDataLon = ProductData.createInstance(new double[width * height]);
        final ProductData productDataLat = ProductData.createInstance(new double[width * height]);

        final VariableDescriptor lonDescriptor = variablesMap.get(LON_VAR_NAME);
        readData(rasterExtract, productDataLon, lonDescriptor, LON_VAR_NAME, false);

        final VariableDescriptor latDescriptor = variablesMap.get(LAT_VAR_NAME);
        readData(rasterExtract, productDataLat, latDescriptor, LAT_VAR_NAME, false);

        // @todo 1 tb/tb read from sensor specific class 2025-02-07
        final double resolutionInKm;
        String productType = product.getProductType();
        if (productType.contains("RR")) {
            resolutionInKm = 1.2;
        } else if (productType.contains("FR")) {
            resolutionInKm = 0.3;
        } else {
            throw new RuntimeException("not foreseen to get here");
        }

        final GeoRaster geoRaster = new GeoRaster((double[]) productDataLon.getElems(), (double[]) productDataLat.getElems(),
                LON_VAR_NAME, LAT_VAR_NAME, lonBand.getRasterWidth(), lonBand.getRasterHeight(), resolutionInKm);

        final String[] codingKeys = getForwardAndInverseKeys_pixelCoding(SYSPROP_OLCI_PIXEL_CODING_INVERSE);
        final ForwardCoding forward = ComponentFactory.getForward(codingKeys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(codingKeys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();

        return geoCoding;
    }

    private ComponentGeoCoding createTiePointGeoCoding(Product product) throws IOException {
        TiePointGrid lonGrid = product.getTiePointGrid(TP_LON_VAR_NAME);
        TiePointGrid latGrid = product.getTiePointGrid(TP_LAT_VAR_NAME);
        if (latGrid == null || lonGrid == null) {
            return null;
        }

        final VariableDescriptor lonDescriptor = tiepointMap.get(TP_LON_VAR_NAME);
        ensureWidthAndHeight(lonDescriptor, manifest);
        final VariableDescriptor latDescriptor = tiepointMap.get(TP_LAT_VAR_NAME);
        ensureWidthAndHeight(latDescriptor, manifest);

        final int width = lonDescriptor.getWidth();
        final int height = lonDescriptor.getHeight();
        final int bufferSize = width * height;
        final ProductData productDataLon = ProductData.createInstance(new double[bufferSize]);
        final ProductData productDataLat = ProductData.createInstance(new double[bufferSize]);

        final RasterExtract rasterExtract = new RasterExtract(0, 0, width, height, 1, 1);
        readData(rasterExtract, productDataLon, lonDescriptor, TP_LON_VAR_NAME, true);
        readData(rasterExtract, productDataLat, latDescriptor, TP_LAT_VAR_NAME, true);

        // @todo 1 tb/tb read from sensor specific class 2025-02-07
        final double resolutionInKm;
        String productType = product.getProductType();
        if (productType.contains("RR")) {
            resolutionInKm = 1.2;
        } else if (productType.contains("FR")) {
            resolutionInKm = 0.3;
        } else {
            throw new RuntimeException("not foreseen to get here");
        }

        final GeoRaster geoRaster = new GeoRaster((double[]) productDataLon.getElems(), (double[]) productDataLat.getElems(), TP_LON_VAR_NAME, TP_LAT_VAR_NAME,
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
            // @todo 2 - add band - check for other attributes (unit, description, fill value) tb 2025-12-18
            final int dataType = ProductData.getType(descriptor.getDataType());
            final String bandname = descriptor.getName();

            final Band band = new BandUsingReaderDirectly(bandname, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());


            product.addBand(band);

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

            final Variable netCDFVariable = getNetCDFVariable(descriptor, bandname);
            addSampleCodings(product, band, netCDFVariable, false);
            //addFillValue(netCDFVariable, band);
            band.setScalingFactor(getScalingFactor(netCDFVariable));
            band.setScalingOffset(getAddOffset(netCDFVariable));
            addFillValue(band, netCDFVariable);

            applyCustomCalibration(band);

            if (OlciProductFactory.isUncertaintyBand(bandname) || OlciProductFactory.isLogScaledGeophysicalData(bandname)) {
                final String unit = descriptor.getUnits();
                if (isLogScaledUnit(unit)) {
                    band.setLog10Scaled(true);
                    band.setUnit(OlciProductFactory.stripLogFromUnit(unit));

                    final String description = descriptor.getDescription();
                    band.setDescription(stripLogFromDescription(description));
                }

                // @todo tb refactor 2025-04-03
                band.setValidPixelExpression( "!quality_flags.invalid");
            } else {
                band.setDescription(descriptor.getDescription());
                band.setUnit(descriptor.getUnits());
            }
        }
    }

    private void applyCustomCalibration(Band band) {
        final Preferences preferences = loadPreferences();
        preferences.getBoolean(OLCI_L1_CUSTOM_CALIBRATION, false);

        final String calibrationOffsetPropertyName = OLCI_L1_CALIBRATION_PATTERN
                .replace("ID", band.getName().toLowerCase())
                .replace("TYPE", "offset");
        final double calibrationOffset = preferences.getDouble(calibrationOffsetPropertyName, Double.NaN);
        if (!Double.isNaN(calibrationOffset)) {
            band.setScalingOffset(calibrationOffset);
        }

        String calibrationFactorPropertyName = OLCI_L1_CALIBRATION_PATTERN
                .replace("ID", band.getName().toLowerCase())
                .replace("TYPE", "factor");
        final double calibrationFactor = preferences.getDouble(calibrationFactorPropertyName, Double.NaN);
        if (!Double.isNaN(calibrationFactor)) {
            band.setScalingFactor(calibrationFactor);
        }
    }

    // @todo 1 tb/tb this is OLCI specific - refactor 2025-02-11
    private Preferences loadPreferences() {
        return Config.instance("opttbx").load().preferences();
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

    private void addSpecialBands(Product product, Manifest manifest) throws IOException {
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
                    addVariableAsBand(product, specialVariable, getLayerName(variableFullName, i), true);
                }
            }
        }
    }

    protected void addVariableAsBand(Product product, Variable variable, String variableName, boolean synthetic) {
        final int type = getRasterDataType(variable);

        final Band band = product.addBand(variableName, type);
        band.setDescription(variable.getDescription());
        band.setUnit(variable.getUnitsString());
        band.setScalingFactor(getScalingFactor(variable));
        band.setScalingOffset(getAddOffset(variable));
        band.setSpectralWavelength(getSpectralWavelength(variable));
        band.setSpectralBandwidth(getSpectralBandwidth(variable));
        band.setSynthetic(synthetic);
        addSampleCodings(product, band, variable, false);
        addFillValue(band, variable);
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    protected void addFillValue(Band band, Variable variable) {
        final Attribute fillValueAttribute = variable.findAttribute(fillValue);
        if (fillValueAttribute != null) {
            //todo double is not always correct
            band.setNoDataValueUsed(!band.isFlagBand());
            band.setNoDataValue(fillValueAttribute.getNumericValue().doubleValue());
            // enable only if it is not a flag band
        }
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    protected void addSampleCodings(Product product, Band band, Variable variable, boolean msb) {
        final Attribute flagValuesAttribute = variable.findAttribute(flag_values);
        final Attribute flagMasksAttribute = variable.findAttribute(flag_masks);
        final Attribute flagMeaningsAttribute = variable.findAttribute(flag_meanings);
        if (flagValuesAttribute != null && flagMasksAttribute != null) {
            final FlagCoding flagCoding =
                    getFlagCoding(product, band.getName(), band.getDescription(), flagMeaningsAttribute,
                            flagValuesAttribute, flagMasksAttribute, msb);
            band.setSampleCoding(flagCoding);
        } else if (flagValuesAttribute != null) {
            final IndexCoding indexCoding =
                    getIndexCoding(product, band.getName(), band.getDescription(), flagMeaningsAttribute,
                            flagValuesAttribute, msb);
            band.setSampleCoding(indexCoding);
        } else if (flagMasksAttribute != null) {
            final FlagCoding flagCoding = getFlagCoding(product, band.getName(), band.getDescription(),
                    flagMeaningsAttribute, flagMasksAttribute, msb);
            band.setSampleCoding(flagCoding);
        }
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private FlagCoding getFlagCoding(Product product, String flagCodingName, String flagCodingDescription,
                                     Attribute flagMeaningsAttribute, Attribute flagValuesAttribute,
                                     Attribute flagMasksAttribute, boolean msb) {
        final FlagCoding flagCoding = new FlagCoding(flagCodingName);
        flagCoding.setDescription(flagCodingDescription);
        addSamples(flagCoding, flagMeaningsAttribute, flagValuesAttribute, flagMasksAttribute, msb);
        if (!product.getFlagCodingGroup().contains(flagCodingName)) {
            product.getFlagCodingGroup().add(flagCoding);
        }
        return flagCoding;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private FlagCoding getFlagCoding(Product product, String flagCodingName, String flagCodingDescription,
                                     Attribute flagMeaningsAttribute, Attribute flagMasksAttribute, boolean msb) {
        final FlagCoding flagCoding = new FlagCoding(flagCodingName);
        flagCoding.setDescription(flagCodingDescription);
        addSamples(flagCoding, flagMeaningsAttribute, flagMasksAttribute, msb);
        if (!product.getFlagCodingGroup().contains(flagCodingName)) {
            product.getFlagCodingGroup().add(flagCoding);
        }
        return flagCoding;
    }

    // @todo 1 tb/tb this is duplicated - refactor! 2025-02-12
    private IndexCoding getIndexCoding(Product product, String indexCodingName, String indexCodingDescription,
                                       Attribute flagMeaningsAttribute, Attribute flagValuesAttribute, boolean msb) {
        final IndexCoding indexCoding = new IndexCoding(indexCodingName);
        indexCoding.setDescription(indexCodingDescription);
        addSamples(indexCoding, flagMeaningsAttribute, flagValuesAttribute, msb);
        if (!product.getIndexCodingGroup().contains(indexCodingName)) {
            product.getIndexCodingGroup().add(indexCoding);
        }
        return indexCoding;
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
                }
                if (variableType == SPECIAL) {
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

        String name = destBand.getName();
        if (isLayerName(name)) {
            name = getVariableNameFromLayerName(destBand.getName());
            descriptor = specialsMap.get(name);
            int layerIndex = getLayerIndexFromLayerName(destBand.getName());
            // get layer from layername
            rasterExtract = new RasterExtract(sourceOffsetX, layerIndex - 1, sourceWidth, 1, 1, 1);
        } else {
            descriptor = variablesMap.get(name);
            rasterExtract = new RasterExtract(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY);
        }

        readData(rasterExtract, destBuffer, descriptor, name, true);
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

        if (descriptor.getVariableType() == SPECIAL) {
            // create line buffer
            final ProductData lineBuffer = ProductData.createInstance(new float[rasterExtract.getWidth()]);
            // read line
            extractSubset(rasterExtract, lineBuffer, rawDataArray, netCDFVariable, rawData);
            // duplicate data over Y dimension to destination buffer
            final float[] targetBuffer = (float[]) destBuffer.getElems();
            final float[] lineBufferElems = (float[]) lineBuffer.getElems();
            final int lineLenght = lineBufferElems.length;
            int offset = 0;
            while ((offset + lineLenght) < targetBuffer.length) {
                int remaining = targetBuffer.length - (offset + lineLenght);
                int toCopy = lineLenght;
                if (remaining < lineLenght) {
                    toCopy = remaining;
                }
                System.arraycopy(lineBufferElems, 0, targetBuffer, offset, toCopy);
                offset += lineLenght;
            }
        } else {
            extractSubset(rasterExtract, destBuffer, rawDataArray, netCDFVariable, rawData);
        }
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
        specialsMap.clear();

        for (final NetcdfFile ncFile : filesMap.values()) {
            ncFile.close();
        }
        filesMap.clear();
        dataMap.clear();

        manifest = null;

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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setMasks(Product targetProduct) {
        final Band[] bands = targetProduct.getBands();
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
                        final Color maskColor = getColorProvider().getMaskColor(sampleName);
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
}
