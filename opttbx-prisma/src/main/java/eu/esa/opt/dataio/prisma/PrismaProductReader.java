package eu.esa.opt.dataio.prisma;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIOException;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoCodingFactory;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.DataNode;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.LineTimeCoding;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.SampleCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.datamodel.TimeCoding;
import org.esa.snap.core.datamodel.VirtualBand;
import org.esa.snap.core.util.DateTimeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.BinaryOperator;

import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_AUTO_GROUPING;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_CUBE_MEASUREMENT_NAME;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.LEVEL_DEPENDENT_CUBE_UNIT;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_PATTERN;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.PRISMA_FILENAME_REGEX;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.VARIABLE_DESCRIPTION;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.VARIABLE_UNIT;
import static eu.esa.opt.dataio.prisma.PrismaConstantsAndUtils.convertToFile;

public class PrismaProductReader extends AbstractProductReader {

    private final Map<String, Array> _varCache = new HashMap<>();
    private final Map<ComparableIntArray, GeoCoding> _geoCodingLookUp = new HashMap<>();
    private final Map<ComparableIntArray, TimeCoding> _timeCodingLookUp = new HashMap<>();
    private final TreeSet<ComparableIntArray> _dimsSet = new TreeSet<>();

    private String _prefixErrorMessage;
    private NetcdfFile _hdfFile;
    private String _productLevel;

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
        File inputFile = convertToFile(getInput());
        assert inputFile != null;
        _prefixErrorMessage = "Unable to open '" + inputFile.getAbsolutePath() + ". ";
        final String fileName = inputFile.getName();
        if (!PRISMA_FILENAME_PATTERN.matcher(fileName).matches()) {
            throw new ProductIOException(_prefixErrorMessage + "The file name: '" + fileName + "' does not match the regular expression: " + PRISMA_FILENAME_REGEX);
        }
        _hdfFile = NetcdfFileOpener.open(inputFile);
        if (_hdfFile == null) {
            throw new ProductIOException(_prefixErrorMessage + "HDF could not be opened.");
        }
        _productLevel = getGlobalAttributeNotNull("Processing_Level").getStringValue();
        final String processorName = getGlobalAttributeNotNull("Processor_Name").getStringValue();
        final String productName = getGlobalAttributeNotNull("Product_Name").getStringValue();

        collectDimensionsAndDatasets(_hdfFile.getRootGroup(), _dimsSet, new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        int sceneRasterWidth = _dimsSet.first().array[1];
        int sceneRasterHeight = _dimsSet.first().array[0];
        final Product product = new Product(productName, "ASI Prisma " + processorName, sceneRasterWidth, sceneRasterHeight);
        product.setProductReader(this);
        product.setFileLocation(inputFile.getAbsoluteFile());
        MetadataReader.readMetadata(_hdfFile, product);

        final Group swathsGroup = _hdfFile.findGroup("/HDFEOS/SWATHS");
        createGeoAndTimeCodings(swathsGroup, product);
        for (Group group : swathsGroup.getGroups()) {
            String key = group.getShortName();
            if (!key.startsWith("PRS_")) {
                continue;
            }
            final String dataGroupName = key.substring(key.lastIndexOf("_") + 1);
            final TreeSet<ComparableIntArray> dimensionsSet = new TreeSet<>();
            final HashMap<String, Variable> datasetMap1D = new HashMap<>();
            final HashMap<String, Variable> tinyDatasetMap2D = new HashMap<>();
            final HashMap<String, Variable> datasetMap2D = new HashMap<>();
            final HashMap<String, Variable> datasetMap3D = new HashMap<>();
            collectDimensionsAndDatasets(group, dimensionsSet, datasetMap1D, tinyDatasetMap2D, datasetMap2D, datasetMap3D);
            final TimeCoding timeCoding = getOrCreateTimeCoding(dataGroupName, datasetMap1D, product);
            final GeoCoding geoCoding = getOrCreateGeoCoding(dataGroupName, datasetMap2D, product, timeCoding);
            for (Variable variable : tinyDatasetMap2D.values()) {
                addSubsampledBand(dataGroupName, product, geoCoding, timeCoding, variable);
            }
            for (Variable variable : datasetMap2D.values()) {
                if (isRedundantLatLon(dataGroupName, variable.getShortName())) {
                    continue;
                }
                addBand(dataGroupName, product, geoCoding, timeCoding, variable);
            }
            for (Variable variable : datasetMap3D.values()) {
                if (isRedundantLatLon(dataGroupName, variable.getShortName())) {
                    continue;
                }
                addCubeBands(dataGroupName, product, geoCoding, timeCoding, variable);
            }
        }

        product.setAutoGrouping(LEVEL_DEPENDENT_AUTO_GROUPING.get(_productLevel));
        final Band[] bands = product.getBands();
        for (Band band : bands) {
            if (band.getGeoCoding() != null && band.getRasterWidth() == sceneRasterWidth && band.getRasterHeight() == sceneRasterHeight) {
                product.setSceneGeoCoding(band.getGeoCoding());
                break;
            }
        }
        final String startTime = getGlobalAttributeNotNull("Product_StartTime").getStringValue();
        final String stopTime = getGlobalAttributeNotNull("Product_StopTime").getStringValue();
        final ProductData.UTC startTimeUtc = DateTimeUtils.parseDate(startTime, "yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        final ProductData.UTC endTimeUtc = DateTimeUtils.parseDate(stopTime, "yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        product.setStartTime(startTimeUtc);
        product.setEndTime(endTimeUtc);

        return product;
    }

    private void createGeoAndTimeCodings(Group swathsGroup, Product product) throws IOException {
        for (Group group : swathsGroup.getGroups()) {
            String key = group.getShortName();
            if (!key.startsWith("PRS_")) {
                continue;
            }
            final String dataGroupName = key.substring(key.lastIndexOf("_") + 1);
            if (!dataGroupName.equals("HCO") && !dataGroupName.equals("PCO")) {
                continue;
            }
            final TreeSet<ComparableIntArray> dimensionsSet = new TreeSet<>();
            final HashMap<String, Variable> datasetMap1D = new HashMap<>();
            final HashMap<String, Variable> tinyDatasetMap2D = new HashMap<>();
            final HashMap<String, Variable> datasetMap2D = new HashMap<>();
            final HashMap<String, Variable> datasetMap3D = new HashMap<>();
            collectDimensionsAndDatasets(group, dimensionsSet, datasetMap1D, tinyDatasetMap2D, datasetMap2D, datasetMap3D);
            final TimeCoding timeCoding = getOrCreateTimeCoding(dataGroupName, datasetMap1D, product);
            getOrCreateGeoCoding(dataGroupName, datasetMap2D, product, timeCoding);
        }
    }

    static boolean isRedundantLatLon(String dataGroupName, String datasetName) {
        return (datasetName.contains("Latitude") || datasetName.contains("Longitude"))
               && !dataGroupName.equals("HCO") && !dataGroupName.equals("PCO");
    }

    private Band addSubsampledBand(String dataGroupName, Product product, GeoCoding geoCoding, TimeCoding timeCoding, Variable variable) throws IOException {
        final String datasetName = variable.getShortName();
        final String bandName = createBandname(dataGroupName, datasetName);
        final Band existingBand = product.getBand(bandName);
        if (existingBand != null) {
            return existingBand;
        }

        final int[] dimensions = variable.getShape();
        final int width = dimensions[1];
        final int height = dimensions[0];
        final Array array;
        try {
            array = variable.read(new int[]{0, 0}, dimensions);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
        final short[] src = (short[]) array.copyTo1DJavaArray();
        final ProductData rawBuffer = ProductData.createUnsignedInstance(src);
        final Band intermediate = new Band("intermediate", rawBuffer.getType(), width, height);
        intermediate.setData(rawBuffer);
        final String scaleName = "L2Scale" + dataGroupName;
        final float scaleMin = getGlobalAttributeNotNull(scaleName + "Min").getNumericValue().floatValue();
        final float scaleMax = getGlobalAttributeNotNull(scaleName + "Max").getNumericValue().floatValue();
        final double scalingFactor = (scaleMax - scaleMin) / 65535;
        intermediate.setScalingFactor(scalingFactor);
        intermediate.setScalingOffset(scaleMin);
        final float[] scaledFloats = intermediate.getPixels(0, 0, width, height, new float[width * height]);
        final TiePointGrid tiePointGrid = new TiePointGrid(bandName + "TP", width, height, 10, 10, 20, 20, scaledFloats);
        product.addTiePointGrid(tiePointGrid);
        final VirtualBand band = new VirtualBand(bandName, ProductData.TYPE_FLOAT32,
                                                 product.getSceneRasterWidth(), product.getSceneRasterHeight(),
                                                 tiePointGrid.getName());
        if (geoCoding != null) {
            band.setGeoCoding(geoCoding);
            tiePointGrid.setGeoCoding(geoCoding);
        }
        if (timeCoding != null) {
            band.setTimeCoding(timeCoding);
            tiePointGrid.setTimeCoding(timeCoding);
        }
        setUnitAndDescription(new DataNode[]{band, tiePointGrid});
        product.addBand(band);
        return band;
    }

    private static void setUnitAndDescription(DataNode[] dataNodes) {
        final String name = dataNodes[0].getName();
        for (DataNode dataNode : dataNodes) {
            if (dataNode instanceof PrismaBand) {
                final PrismaBand prismaBand = (PrismaBand) dataNode;
                final Variable variable = prismaBand.variable;
                final String attValue = variable.findAttValueIgnoreCase("units", "");
                if (attValue.length() > 0) {
                    prismaBand.setUnit(attValue);
                }
            }
            if (VARIABLE_UNIT.containsKey(name)) {
                dataNode.setUnit(VARIABLE_UNIT.get(name));
            }
            if (VARIABLE_DESCRIPTION.containsKey(name)) {
                dataNode.setDescription(VARIABLE_DESCRIPTION.get(name));
            }
        }
    }

    private Band addBand(String dataGroupName, Product product, GeoCoding geoCoding, TimeCoding timeCoding, Variable variable) throws ProductIOException {
        final String varName = variable.getShortName();
        final String bandName = createBandname(dataGroupName, varName);
        final Band existingBand = product.getBand(bandName);
        if (existingBand != null) {
            return existingBand;
        }

        final int[] dimensions = variable.getShape();
        final int width = dimensions[1];
        final int height = dimensions[0];
        final DataType dataType = variable.getDataType();
        final int productDataType = getProductDataType(dataType);
        final Band band = new PrismaBand(bandName, productDataType, width, height, variable);
        if (geoCoding != null) {
            band.setGeoCoding(geoCoding);
        }

        final SampleCoding sampleCoding;
        if (product.getIndexCodingGroup().contains(varName)) {
            sampleCoding = product.getIndexCodingGroup().get(varName);
        } else {
            sampleCoding = PrismaConstantsAndUtils.createSampleCoding(product, varName);
        }
        if (sampleCoding != null) {
            band.setSampleCoding(sampleCoding);
        }
        final boolean measurementCube = varName.toLowerCase().contains("cube");
        if (measurementCube) {
            final double scalingFactor;
            final double scalingOffset;
            if ("1".equals(_productLevel)) {
                scalingFactor = getGlobalAttributeNotNull("ScaleFactor_Pan").getNumericValue().doubleValue();
                scalingOffset = getGlobalAttributeNotNull("Offset_Pan").getNumericValue().doubleValue();
            } else {
                final float scaleMin = getGlobalAttributeNotNull("L2ScalePanMin").getNumericValue().floatValue();
                final float scaleMax = getGlobalAttributeNotNull("L2ScalePanMax").getNumericValue().floatValue();
                scalingFactor = (scaleMax - scaleMin) / 65535;
                scalingOffset = scaleMin;
            }
            band.setScalingFactor(scalingFactor);
            band.setScalingOffset(scalingOffset);
            band.setUnit("1");
        }
        setUnitAndDescription(new DataNode[]{band});
        product.addBand(band);
        return band;
    }

    private static String createBandname(String dataGroupName, String datasetName) {
        final String bandName;
        if (datasetName.startsWith(dataGroupName + "_")) {
            bandName = datasetName;
        } else {
            bandName = dataGroupName + "_" + datasetName;
        }
        return bandName;
    }

    private GeoCoding getOrCreateGeoCoding(String dataGroupName, HashMap<String, Variable> datasetMap2D, Product product, TimeCoding timeCoding) throws IOException {
        final Attribute epsgCodeAtt = getGlobalAttribute("Epsg_Code");
        final Variable latDS;
        final Variable lonDS;
        if (datasetMap2D.containsKey("Latitude") && datasetMap2D.containsKey("Longitude")) {
            latDS = datasetMap2D.get("Latitude");
            lonDS = datasetMap2D.get("Longitude");
        } else if (datasetMap2D.containsKey("Latitude_SWIR") && datasetMap2D.containsKey("Longitude_SWIR")) {
            latDS = datasetMap2D.get("Latitude_SWIR");
            lonDS = datasetMap2D.get("Longitude_SWIR");
        } else if (datasetMap2D.containsKey("Latitude_VNIR") && datasetMap2D.containsKey("Longitude_VNIR")) {
            latDS = datasetMap2D.get("Latitude_VNIR");
            lonDS = datasetMap2D.get("Longitude_VNIR");
        } else {
            return null;
        }
        final int[] dimensions = latDS.getShape();
        final ComparableIntArray lookUpKey = new ComparableIntArray(dimensions);
        if (_geoCodingLookUp.containsKey(lookUpKey)) {
            return _geoCodingLookUp.get(lookUpKey);
        }
        if (epsgCodeAtt != null) {
            final int width = dimensions[1];
            final int height = dimensions[0];

            final long epsgCodeValue = epsgCodeAtt.getNumericValue().longValue();

            String[] nameParts = {"Product_", "easting"};
            float minEasting = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxEasting = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            nameParts = new String[]{"Product_", "northing"};
            float minNorthing = getComputedGlobalAttValue(nameParts, Float.MAX_VALUE, Math::min);
            float maxNorthing = getComputedGlobalAttValue(nameParts, Float.MIN_VALUE, Math::max);

            double scaleX = (maxEasting - minEasting) / (width - 1);
            double scaleY = (maxNorthing - minNorthing) / (height - 1);

            AffineTransform i2m = new AffineTransform();
            i2m.translate(minEasting, maxNorthing);
            i2m.scale(scaleX, -scaleY);

            String epsgCode = "EPSG:" + epsgCodeValue;
            try {
                CoordinateReferenceSystem mapCRS = CRS.decode(epsgCode);
                final CrsGeoCoding geoCoding = new CrsGeoCoding(mapCRS, width, height, minEasting, maxNorthing, scaleX, scaleY);
                _geoCodingLookUp.put(lookUpKey, geoCoding);
                return geoCoding;
            } catch (FactoryException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not decode EPSG code: " + epsgCodeValue, e);
            } catch (TransformException e) {
                throw new RuntimeException(_prefixErrorMessage + "Could not create CrsGeoCoding.", e);
            }
        }
        final Band latBand = addBand(dataGroupName, product, null, timeCoding, latDS);
        final Band lonBand = addBand(dataGroupName, product, null, timeCoding, lonDS);
        final ComponentGeoCoding pixelGeoCoding = GeoCodingFactory.createPixelGeoCoding(latBand, lonBand);
        latBand.setGeoCoding(pixelGeoCoding);
        lonBand.setGeoCoding(pixelGeoCoding);
        _geoCodingLookUp.put(lookUpKey, pixelGeoCoding);
        return pixelGeoCoding;
    }

    private TimeCoding getOrCreateTimeCoding(String dataGroupName, HashMap<String, Variable> datasetMap1D, Product product) throws IOException {
        if (!datasetMap1D.containsKey("Time")) {
            return null;
        }
        final Variable time = datasetMap1D.get("Time");
        final int[] dimensions = time.getShape();
        final ComparableIntArray lookUpKey = new ComparableIntArray(dimensions);
        if (_timeCodingLookUp.containsKey(lookUpKey)) {
            return _timeCodingLookUp.get(lookUpKey);
        }
        final double[] doubles;
        synchronized (_hdfFile) {
            doubles = (double[]) time.read().copyTo1DJavaArray();
        }
        final LineTimeCoding timeCoding = new LineTimeCoding(doubles);
        _timeCodingLookUp.put(lookUpKey, timeCoding);
        return timeCoding;
    }

    private float getComputedGlobalAttValue(String[] nameParts, float compareValue, BinaryOperator<Float> mathOperator) {
        for (Attribute attribute : _hdfFile.getGlobalAttributes()) {
            final String name = attribute.getShortName();
            if (Arrays.stream(nameParts).allMatch(name::contains)) {
                compareValue = mathOperator.apply(compareValue, attribute.getNumericValue().floatValue());
            }
        }
        return compareValue;
    }

    private void addCubeBands(String dataGroupName, Product product, GeoCoding geoCoding, TimeCoding timeCoding, Variable cubeVar) throws ProductIOException {
        final String varName = cubeVar.getShortName();
        final String varNameLC = varName.toLowerCase();
        final boolean measurementCube = varNameLC.contains("cube");
        if (measurementCube) {
            final String measurementName = LEVEL_DEPENDENT_CUBE_MEASUREMENT_NAME.get(_productLevel);
            final String cubeUnit = LEVEL_DEPENDENT_CUBE_UNIT.get(_productLevel);
            final String cubeName = varNameLC.contains("vnir") ? "Vnir" : "Swir";
            final Array wavelengths = getGlobalAttributeNotNull("List_Cw_" + cubeName).getValues();
            final Array bandwidths = getGlobalAttributeNotNull("List_Fwhm_" + cubeName).getValues();
            final int[] dimensions = cubeVar.getShape();
            final int width = dimensions[2];
            final int height = dimensions[0];
            final int numBands = dimensions[1];
            final int numDigits = ("" + numBands).length();
            final String autogrouping = dataGroupName + "_" + cubeName;
            final String bandNameFormatExpression = autogrouping + measurementName + "_%0" + numDigits + "d";
            final DataType dataType = cubeVar.getDataType();
            final int productDataType = getProductDataType(dataType);
            final double scalingFactor;
            final double scalingOffset;
            if ("1".equals(_productLevel)) {
                final String factorName = "ScaleFactor_" + cubeName;
                final String offsetName = "Offset_" + cubeName;
                scalingOffset = getGlobalAttributeNotNull(offsetName).getNumericValue().doubleValue();
                scalingFactor = getGlobalAttributeNotNull(factorName).getNumericValue().doubleValue();
            } else {
                final String scaleName = "L2Scale" + cubeName;
                final float scaleMin = getGlobalAttributeNotNull(scaleName + "Min").getNumericValue().floatValue();
                final float scaleMax = getGlobalAttributeNotNull(scaleName + "Max").getNumericValue().floatValue();
                scalingOffset = scaleMin;
                if (ProductData.TYPE_UINT16 == productDataType) {
                    scalingFactor = (scaleMax - scaleMin) / 65535;
                } else {
                    throw new ProductIOException(_prefixErrorMessage + "Unsupported cube data type.");
                }
            }
            for (int i = 0; i < numBands; i++) {
                final float wavelength = wavelengths.getFloat(i);
                if (wavelength == 0.0f) {
                    continue;
                }
                String bandName = String.format(bandNameFormatExpression, i + 1);
                final Band band = new PrismaBand(bandName, productDataType, width, height, cubeVar, i);
                band.setSpectralWavelength(wavelength);
                band.setSpectralBandwidth(bandwidths.getFloat(i));
                band.setScalingFactor(scalingFactor);
                band.setScalingOffset(scalingOffset);
                band.setUnit(cubeUnit);
                if (geoCoding != null) {
                    band.setGeoCoding(geoCoding);
                }
                if (timeCoding != null) {
                    band.setTimeCoding(timeCoding);
                }
                product.addBand(band);
            }
        } else {
            final int[] dimensions = cubeVar.getShape();
            final String cubeName = varNameLC.contains("vnir") ? "Vnir" :
                    varNameLC.contains("swir") ? "Swir" : null;
            final Array wavelengths;
            if (cubeName != null) {
                wavelengths = getGlobalAttributeNotNull("List_Cw_" + cubeName).getValues();
            } else {
                wavelengths = null;
            }

            final int width = dimensions[2];
            final int height = dimensions[0];
            final int numBands = dimensions[1];
            final int numDigits = ("" + numBands).length();
            final String autogrouping = dataGroupName + "_" + varName;
            final String bandNameFormatExpression = autogrouping + "_%0" + numDigits + "d";
            final DataType dataType = cubeVar.getDataType();
            final int productDataType = getProductDataType(dataType);
            final SampleCoding sampleCoding;
            if (product.getIndexCodingGroup().contains(varName)) {
                sampleCoding = product.getIndexCodingGroup().get(varName);
            } else {
                sampleCoding = PrismaConstantsAndUtils.createSampleCoding(product, varName);
            }
            for (int i = 0; i < numBands; i++) {
                if (wavelengths != null && wavelengths.getFloat(i) == 0.0f) {
                    continue;
                }
                String bandName = String.format(bandNameFormatExpression, i + 1);
                final Band band = new PrismaBand(bandName, productDataType, width, height, cubeVar, i) {
                    @Override
                    public boolean isProductReaderDirectlyUsable() {
                        return true;
                    }
                };
                if (geoCoding != null) {
                    band.setGeoCoding(geoCoding);
                }
                if (timeCoding != null) {
                    band.setTimeCoding(timeCoding);
                }
                if (sampleCoding != null) {
                    band.setSampleCoding(sampleCoding);
                }
                product.addBand(band);
            }
        }
    }

    private int getProductDataType(DataType dataType) throws ProductIOException {
        int productDataType;
        if (dataType.isFloatingPoint()) {
            final int numBytes = dataType.getSize();
            if (numBytes == 4) {
                productDataType = ProductData.TYPE_FLOAT32;
            } else if (numBytes == 8) {
                productDataType = ProductData.TYPE_FLOAT64;
            } else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
            }
        } else if (dataType.isIntegral()) {
            final boolean isUnsigned = dataType.isUnsigned();
            final int numBytes = dataType.getSize();
            if (numBytes == 1) {
                productDataType = isUnsigned ? ProductData.TYPE_UINT8 : ProductData.TYPE_INT8;
            } else if (numBytes == 2) {
                productDataType = isUnsigned ? ProductData.TYPE_UINT16 : ProductData.TYPE_INT16;
            } else if (numBytes == 4) {
                productDataType = isUnsigned ? ProductData.TYPE_UINT32 : ProductData.TYPE_INT32;
            } else if (numBytes == 8) {
                productDataType = isUnsigned ? ProductData.TYPE_UINT64 : ProductData.TYPE_INT64;
            } else {
                throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
            }
        } else {
            throw new ProductIOException(_prefixErrorMessage + "Unsupported data type.");
        }
        return productDataType;
    }

    private Attribute getGlobalAttributeNotNull(String attributeName) throws ProductIOException {
        final Attribute attribute = getGlobalAttribute(attributeName);
        if (attribute == null) {
            throw new ProductIOException(_prefixErrorMessage + "Global Attribute '" + attributeName + "' not found.");
        }
        return attribute;
    }

    private Attribute getGlobalAttribute(String attributeName) {
        return _hdfFile.findGlobalAttribute(attributeName);
    }

    @Override
    protected void readBandRasterDataImpl(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            Band destBand,
            int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer, ProgressMonitor pm) throws IOException {

        final PrismaBand prismaBand = (PrismaBand) destBand;
        if (prismaBand.cubeIndex > -1) {
            readFromCubeVariable(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                                 prismaBand, destWidth, destHeight, destBuffer, pm);
        } else {
            readFromVariable(sourceOffsetX, sourceOffsetY, sourceWidth, sourceHeight, sourceStepX, sourceStepY,
                             prismaBand, destWidth, destHeight, destBuffer, pm);
        }
        pm.worked(sourceWidth * sourceHeight);
    }

    private void readFromVariable(
            int srcOffsetX, int srcOffsetY, int srcWidth, int srcHeight, int srcStepX, int srcStepY,
            PrismaBand destBand, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) throws IOException {
        final Variable variable = destBand.variable;
        final String variableName = variable.getFullName();
        if ("1".equals(_productLevel) && !_varCache.containsKey(variableName)) {
            synchronized (_hdfFile) {
                _varCache.put(variableName, variable.read());
            }
        }

        final int[] sliceOffset = {srcOffsetY, srcOffsetX};
        final int[] sliceDimensions = {srcHeight, srcWidth};
        final int[] stride = {srcStepY, srcStepX};
        try {
            final Object sliceData;
            if (_varCache.containsKey(variableName)) {
                sliceData = _varCache.get(variableName).section(sliceOffset, sliceDimensions, stride).copyTo1DJavaArray();
            } else {
                final Section sect = new Section(sliceOffset, sliceDimensions, stride);
                synchronized (_hdfFile) {
                    sliceData = variable.read(sect).copyTo1DJavaArray();
                }
            }
            destBuffer.setElems(sliceData);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    private void readFromCubeVariable(
            int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
            PrismaBand destBand, int destWidth, int destHeight, ProductData destBuffer,
            ProgressMonitor pm) throws IOException {
        final Variable cubeVar = destBand.variable;
        final String variableName = cubeVar.getFullName();
        final int cubeIndex = destBand.cubeIndex;
        if ("1".equals(_productLevel) && variableName.contains("Cube") && !_varCache.containsKey(variableName)) {
            synchronized (_hdfFile) {
                final Array dataArray = cubeVar.read();
                _varCache.put(variableName, dataArray);
            }
        }
        final int[] sliceOffset = new int[]{sourceOffsetY, cubeIndex, sourceOffsetX};
        final int[] sliceDimensions = new int[]{sourceHeight, 1, sourceWidth};
        final int[] stride = new int[]{sourceStepY, 1, sourceStepX};
        try {
            final Object sliceData;
            if (_varCache.containsKey(variableName)) {
                sliceData = _varCache.get(variableName).section(sliceOffset, sliceDimensions, stride).copyTo1DJavaArray();
            } else {
                final Section sect = new Section(sliceOffset, sliceDimensions, stride);
                synchronized (_hdfFile) {
                    sliceData = cubeVar.read(sect).copyTo1DJavaArray();
                }
            }
            destBuffer.setElems(sliceData);
        } catch (InvalidRangeException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        _dimsSet.clear();
        _geoCodingLookUp.clear();
        _varCache.clear();
        _hdfFile.close();
        _hdfFile = null;
    }

    private static void collectDimensionsAndDatasets(Group group, TreeSet<ComparableIntArray> dimensionsSet,
                                                     Map<String, Variable> datasetMap1D,
                                                     Map<String, Variable> tinyDatasetMap2D,
                                                     Map<String, Variable> datasetMap2D,
                                                     Map<String, Variable> datasetMap3D) {
        final List<Group> groups = group.getGroups();
        for (Group g : groups) {
            collectDimensionsAndDatasets(g, dimensionsSet, datasetMap1D, tinyDatasetMap2D, datasetMap2D, datasetMap3D);
        }
        final List<Variable> variables = group.getVariables();
        for (Variable variable : variables) {
            final int[] shape = variable.getShape();
            final String varName = variable.getShortName();
            if (shape.length == 1 || shape.length == 2 && Arrays.stream(shape).anyMatch(v -> v == 1)) {
                datasetMap1D.put(varName, variable);
            } else if (shape.length >= 2 && shape[0] > 256 && shape[shape.length - 1] > 256) {
                dimensionsSet.add(new ComparableIntArray(shape));
                if (shape.length == 2) {
                    datasetMap2D.put(varName, variable);
                } else {
                    datasetMap3D.put(varName, variable);
                }
            } else {
                tinyDatasetMap2D.put(varName, variable);
            }
        }
    }

    private static class ComparableIntArray implements Comparable<ComparableIntArray> {
        private final int[] array;

        public ComparableIntArray(int[] array) {
            this.array = array;
        }

        public int[] getArray() {
            return array;
        }

        @Override
        public int compareTo(ComparableIntArray o) {
            final int[] other = o.getArray();
            if (array.length != other.length) {
                return Integer.compare(array.length, other.length);
            }
            for (int i = 0; i < array.length; i++) {
                if (array[i] != other[i]) {
                    return Integer.compare(array[i], other[i]);
                }
            }
            return 0;
        }

        @Override
        public String toString() {
            return Arrays.toString(array);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ComparableIntArray that = (ComparableIntArray) o;
            return Objects.deepEquals(array, that.array);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(array);
        }

    }

    private static class PrismaBand extends Band {
        public final Variable variable;
        public final int cubeIndex;

        public PrismaBand(String bandName, int productDataType, int width, int height, Variable variable) {
            this(bandName, productDataType, width, height, variable, -1);
        }

        public PrismaBand(String bandName, int productDataType, int width, int height, Variable variable, int cubeIndex) {
            super(bandName, productDataType, width, height);
            this.variable = variable;
            this.cubeIndex = cubeIndex;
        }

        @Override
        public boolean isProductReaderDirectlyUsable() {
            return true;
        }
    }
}
