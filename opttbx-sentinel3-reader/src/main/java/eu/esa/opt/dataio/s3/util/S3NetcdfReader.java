package eu.esa.opt.dataio.s3.util;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import eu.esa.snap.core.datamodel.band.SparseDataBand;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.dataio.netcdf.util.Constants;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import org.esa.snap.dataio.netcdf.util.NetcdfFileOpener;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Tonio Fincke
 */
public class S3NetcdfReader extends AbstractProductReader {

    //todo check whether this class is still necessary as most of its functionality is also provided by the default netcdf reader
    private static final String product_type = "product_type";

    private NetcdfFile netcdfFile;

    public S3NetcdfReader() {
        super(null);
    }

    public String[] getSuffixesForSeparatingDimensions() {
        return new String[0];
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        File inputFile = getInputFile();
        netcdfFile = NetcdfFileOpener.open(inputFile);

        if (netcdfFile == null) {
            throw new IOException(String.format("Not able to read file '%s'. Might be corrupted.", inputFile));
        }

        final String productType = readProductType();
        int productWidth = getWidth();
        int productHeight = getHeight();

        final Product product = new Product(FileUtils.getFilenameWithoutExtension(inputFile), productType, productWidth, productHeight);
        product.setFileLocation(inputFile);
        addGlobalMetadata(product);
        addBands(product);
        addGeoCoding(product);
        for (final Band band : product.getBands()) {
            if (band instanceof VirtualBand || band instanceof SparseDataBand) {
                continue;
            }
            RenderedImage sourceImage = createSourceImage(band);
            if (product.getPreferredTileSize() == null) {
                product.setPreferredTileSize(sourceImage.getTileWidth(), sourceImage.getTileHeight());
            }
            band.setSourceImage(sourceImage);
        }
        return product;
    }

    @Override
    public void close() throws IOException {
        if (netcdfFile != null) {
            netcdfFile.close();
            netcdfFile = null;
        }
        super.close();
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY,
                                          Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight, ProductData destBuffer,
                                          ProgressMonitor pm) throws IOException {
        throw new IllegalStateException("All bands use images as source for its data, readBandRasterDataImpl should not be called.");
    }

    protected void addGeoCoding(Product product) {

    }

    private File getInputFile() {
        final Object input = getInput();

        if (input instanceof String) {
            return new File((String) input);
        }
        if (input instanceof File) {
            return (File) input;
        }

        throw new IllegalArgumentException(MessageFormat.format("Illegal input: {0}", input));
    }

    protected String[] getSeparatingDimensions() {
        return new String[0];
    }

    protected String[][] getRowColumnNamePairs() {
        return new String[][]{{"rows", "columns"}, {"tie_rows", "tie_columns"}};
    }

    protected RenderedImage createSourceImage(Band band) {
        final String bandName = band.getName();
        String variableName = bandName;
        if (variableName.endsWith("_lsb")) {
            variableName = variableName.substring(0, variableName.indexOf("_lsb"));
        } else if (variableName.endsWith("_msb")) {
            variableName = variableName.substring(0, variableName.indexOf("_msb"));
        }
        Variable variable = null;
        List<String> dimensionNameList = new ArrayList<>();
        List<Integer> dimensionIndexList = new ArrayList<>();
        final String[] separatingDimensions = getSeparatingDimensions();
        final String[] suffixesForSeparatingThirdDimensions = getSuffixesForSeparatingDimensions();
        int lowestSuffixIndex = Integer.MAX_VALUE;
        for (int i = 0; i < separatingDimensions.length; i++) {
            final String dimension = separatingDimensions[i];
            final String suffix = suffixesForSeparatingThirdDimensions[i];
            if (bandName.contains(suffix)) {
                final int suffixIndex = bandName.indexOf(suffix) - 1;
                if (suffixIndex < lowestSuffixIndex) {
                    lowestSuffixIndex = suffixIndex;
                }
                dimensionNameList.add(dimension);
                dimensionIndexList.add(getDimensionIndexFromBandName(bandName));
            }
        }
        if (lowestSuffixIndex < bandName.length()) {
            variableName = bandName.substring(0, lowestSuffixIndex);
            variable = netcdfFile.findVariable(variableName);
        }
        if (variable == null) {
            variable = netcdfFile.findVariable(variableName);
        }
        Dimension widthDimension = getWidthDimension();
        int xIndex = -1;
        int yIndex = -1;
        if (widthDimension != null) {
            xIndex = variable.findDimensionIndex(widthDimension.getFullName());
        }
        Dimension heightDimension = getHeightDimension();
        if (heightDimension != null) {
            yIndex = variable.findDimensionIndex(heightDimension.getFullName());
        }
        String[] dimensionNames = dimensionNameList.toArray(new String[dimensionNameList.size()]);
        int[] dimensionIndexes = new int[dimensionIndexList.size()];
        for (int i = 0; i < dimensionIndexList.size(); i++) {
            dimensionIndexes[i] = dimensionIndexList.get(i);
        }
        S3MultiLevelOpSource levelSource = new S3MultiLevelOpSource(band, variable, dimensionNames, dimensionIndexes, xIndex,
                yIndex);
        return new DefaultMultiLevelImage(levelSource);
    }

    protected int getDimensionIndexFromBandName(String bandName) {
        return Integer.parseInt(bandName.substring(bandName.lastIndexOf("_") + 1)) - 1;
    }

    private void addGlobalMetadata(Product product) {
        final MetadataElement globalAttributesElement = new MetadataElement("Global_Attributes");
        final List<Attribute> globalAttributes = netcdfFile.getGlobalAttributes();
        for (final Attribute attribute : globalAttributes) {
            if (attribute.getValues() != null) {
                final ProductData attributeData = getAttributeData(attribute);
                final MetadataAttribute metadataAttribute = new MetadataAttribute(attribute.getFullName(), attributeData, true);
                globalAttributesElement.addAttribute(metadataAttribute);
            }
        }
        product.getMetadataRoot().addElement(globalAttributesElement);
        final MetadataElement variableAttributesElement = new MetadataElement("Variable_Attributes");
        product.getMetadataRoot().addElement(variableAttributesElement);
    }

    protected void addBands(Product product) {
        final List<Variable> variables = netcdfFile.getVariables();
        for (final Variable variable : variables) {
            final String[][] rowColumnNamePairs = getRowColumnNamePairs();
            for (String[] rowColumnNamePair : rowColumnNamePairs) {
                if (variable.findDimensionIndex(rowColumnNamePair[0]) != -1 &&
                        variable.findDimensionIndex(rowColumnNamePair[1]) != -1) {
                    final String variableName = variable.getFullName();
                    final String[] dimensions = getSeparatingDimensions();
                    final String[] suffixes = getSuffixesForSeparatingDimensions();
                    boolean variableMustStillBeAdded = true;
                    for (int i = 0; i < dimensions.length; i++) {
                        String dimensionName = dimensions[i];
                        if (variable.findDimensionIndex(dimensionName) != -1) {
                            final Dimension dimension =
                                    variable.getDimension(variable.findDimensionIndex(dimensionName));
                            for (int j = 0; j < dimension.getLength(); j++) {
                                addVariableAsBand(product, variable, variableName + "_" + suffixes[i] + "_" + (j + 1), false);
                            }
                            variableMustStillBeAdded = false;
                            break;
                        }
                    }
                    if (variableMustStillBeAdded) {
                        addVariableAsBand(product, variable, variableName, false);
                    }
                }
            }
            addVariableMetadata(variable, product);
        }
    }

    protected void addVariableAsBand(Product product, Variable variable, String variableName, boolean synthetic) {
        int type = S3Util.getRasterDataType(variable);
        //todo consider unsigned long - split into three bands?
        if (type == ProductData.TYPE_INT64 || type == ProductData.TYPE_UINT64) {
            final Band lowerBand = product.addBand(variableName + "_lsb", ProductData.TYPE_UINT32);
            lowerBand.setDescription(variable.getDescription() + "(least significant bytes)");
            lowerBand.setUnit(variable.getUnitsString());
            lowerBand.setScalingFactor(S3Util.getScalingFactor(variable));
            lowerBand.setScalingOffset(S3Util.getAddOffset(variable));
            lowerBand.setSpectralWavelength(S3Util.getSpectralWavelength(variable));
            lowerBand.setSpectralBandwidth(S3Util.getSpectralBandwidth(variable));
            lowerBand.setSynthetic(synthetic);
            S3Util.addSampleCodings(product, lowerBand, variable, false);
            S3Util.addFillValue(lowerBand, variable);

            final Band upperBand = product.addBand(variableName + "_msb", ProductData.TYPE_UINT32);
            upperBand.setDescription(variable.getDescription() + "(most significant bytes)");
            upperBand.setUnit(variable.getUnitsString());
            upperBand.setScalingFactor(S3Util.getScalingFactor(variable));
            upperBand.setScalingOffset(S3Util.getAddOffset(variable));
            upperBand.setSpectralWavelength(S3Util.getSpectralWavelength(variable));
            upperBand.setSpectralBandwidth(S3Util.getSpectralBandwidth(variable));
            upperBand.setSynthetic(synthetic);
            S3Util.addSampleCodings(product, upperBand, variable, true);
            S3Util.addFillValue(upperBand, variable);
        } else {
            final Band band = product.addBand(variableName, type);
            band.setDescription(variable.getDescription());
            band.setUnit(variable.getUnitsString());
            band.setScalingFactor(S3Util.getScalingFactor(variable));
            band.setScalingOffset(S3Util.getAddOffset(variable));
            band.setSpectralWavelength(S3Util.getSpectralWavelength(variable));
            band.setSpectralBandwidth(S3Util.getSpectralBandwidth(variable));
            band.setSynthetic(synthetic);
            S3Util.addSampleCodings(product, band, variable, false);
            S3Util.addFillValue(band, variable);
        }
    }

    protected void addVariableMetadata(Variable variable, Product product) {
        List<Dimension> variableDimensions = variable.getDimensions();
        if (variableDimensions.size() == 1) {
            final MetadataElement variableElement = extractMetadata(variable);
            product.getMetadataRoot().getElement("Variable_Attributes").addElement(variableElement);
        }
    }

    public static MetadataElement extractMetadata(Variable variable) {
        final MetadataElement variableElement = new MetadataElement(variable.getFullName());
        final List<Attribute> attributes = variable.getAttributes();
        for (Attribute attribute : attributes) {
            if (attribute.getFullName().equals(CFConstants.FLAG_MEANINGS)) {
                final String[] flagMeanings = attribute.getStringValue().split(" ");
                for (int i = 0; i < flagMeanings.length; i++) {
                    String flagMeaning = flagMeanings[i];
                    final ProductData attributeData = ProductData.createInstance(flagMeaning);
                    final MetadataAttribute metadataAttribute =
                            new MetadataAttribute(attribute.getFullName() + "." + i, attributeData, true);
                    variableElement.addAttribute(metadataAttribute);
                }
            } else {
                if (attribute.getValues() != null) {
                    final ProductData attributeData = getAttributeData(attribute);
                    final MetadataAttribute metadataAttribute = new MetadataAttribute(attribute.getFullName(), attributeData, true);
                    variableElement.addAttribute(metadataAttribute);
                }
            }
        }
        if (variable.getDataType() != DataType.STRING) {
            try {
                Object data = variable.read().copyTo1DJavaArray();
                MetadataAttribute variableAttribute = null;
                if (data instanceof float[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((float[]) data), true);
                } else if (data instanceof double[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((double[]) data), true);
                } else if (data instanceof byte[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((byte[]) data), true);
                } else if (data instanceof short[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((short[]) data), true);
                } else if (data instanceof int[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((int[]) data), true);
                } else if (data instanceof long[]) {
                    variableAttribute = new MetadataAttribute("value", ProductData.createInstance((long[]) data), true);
                }
                if (variableAttribute != null) {
                    variableAttribute.setUnit(variable.getUnitsString());
                    variableAttribute.setDescription(variable.getDescription());
                    variableElement.addAttribute(variableAttribute);
                }
            } catch (IOException e) {
                Logger logger = Logger.getLogger(S3NetcdfReader.class.getName());
                logger.severe("Could not read variable " + variable.getFullName());
            }
        }
        return variableElement;
    }

    public static int getProductDataType(Attribute attribute) {
        return DataTypeUtils.getEquivalentProductDataType(attribute.getDataType(), false, false);
    }

    public static ProductData getAttributeData(Attribute attribute) {
        int type = getProductDataType(attribute);
        final Array attributeValues = attribute.getValues();
        ProductData productData = null;
        switch (type) {
            case ProductData.TYPE_ASCII: {
                productData = ProductData.createInstance(attributeValues.toString());
                break;
            }
            case ProductData.TYPE_INT8: {
                productData = ProductData.createInstance((byte[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            case ProductData.TYPE_UINT8: {
                Object array = convertShortToByteArray(attributeValues.copyTo1DJavaArray());
                productData = ProductData.createUnsignedInstance((byte[]) array);
                break;
            }
            case ProductData.TYPE_INT16: {
                productData = ProductData.createInstance((short[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            case ProductData.TYPE_UINT16: {
                Object array = convertIntToShortArray(attributeValues.copyTo1DJavaArray());
                productData = ProductData.createUnsignedInstance((short[]) array);
                break;
            }
            case ProductData.TYPE_INT32: {
                productData = ProductData.createInstance((int[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            case ProductData.TYPE_UINT32: {
                Object array = convertLongToIntArray(attributeValues.copyTo1DJavaArray());
                productData = ProductData.createUnsignedInstance((int[]) array);
                break;
            }
            case ProductData.TYPE_INT64: {
                productData = ProductData.createInstance((long[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            case ProductData.TYPE_FLOAT32: {
                productData = ProductData.createInstance((float[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            case ProductData.TYPE_FLOAT64: {
                productData = ProductData.createInstance((double[]) attributeValues.copyTo1DJavaArray());
                break;
            }
            default: {
                break;
            }
        }
        return productData;
    }

    public static Object convertShortToByteArray(Object array) {
        if (array instanceof short[] shortArray) {
            byte[] newArray = new byte[shortArray.length];
            for (int i = 0; i < shortArray.length; i++) {
                newArray[i] = (byte) shortArray[i];
            }
            array = newArray;
        }
        return array;
    }

    public static Object convertIntToShortArray(Object array) {
        if (array instanceof int[] intArray) {
            short[] newArray = new short[intArray.length];
            for (int i = 0; i < intArray.length; i++) {
                newArray[i] = (short) intArray[i];
            }
            array = newArray;
        }
        return array;
    }

    public static Object convertLongToIntArray(Object array) {
        if (array instanceof long[] longArray) {
            int[] newArray = new int[longArray.length];
            for (int i = 0; i < longArray.length; i++) {
                newArray[i] = (int) longArray[i];
            }
            array = newArray;
        }
        return array;
    }

    private int getWidth() {
        Dimension widthDimension = getWidthDimension();
        if (widthDimension != null) {
            return widthDimension.getLength();
        }
        return 0;
    }

    private Dimension getWidthDimension() {
        final String[][] rowColumnNamePairs = getRowColumnNamePairs();
        for (String[] rowColumnNamePair : rowColumnNamePairs) {
            final Dimension widthDimension = netcdfFile.findDimension(rowColumnNamePair[1]);
            if (widthDimension != null) {
                return widthDimension;
            }
        }
        return null;
    }

    private int getHeight() {
        Dimension heightDimension = getHeightDimension();
        if (heightDimension != null) {
            return heightDimension.getLength();
        }
        return 0;
    }

    private Dimension getHeightDimension() {
        final String[][] rowColumnNamePairs = getRowColumnNamePairs();
        for (String[] rowColumnNamePair : rowColumnNamePairs) {
            final Dimension heightDimension = netcdfFile.findDimension(rowColumnNamePair[0]);
            if (heightDimension != null) {
                return heightDimension;
            }
        }
        return null;
    }

    private String readProductType() {
        Attribute typeAttribute = netcdfFile.findGlobalAttribute(product_type);
        if (typeAttribute == null) {
            typeAttribute = netcdfFile.findGlobalAttribute("Conventions");
        }
        String type = null;
        if (typeAttribute != null) {
            type = typeAttribute.getStringValue();
        }
        String productType = Constants.FORMAT_NAME;
        if (type != null && type.trim().length() > 0) {
            productType = type.trim();
        }
        return productType;
    }

    protected NetcdfFile getNetcdfFile() {
        return netcdfFile;
    }

}
