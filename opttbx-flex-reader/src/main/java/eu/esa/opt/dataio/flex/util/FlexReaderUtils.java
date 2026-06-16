package eu.esa.opt.dataio.flex.util;


import eu.esa.opt.dataio.flex.FlexProductReader;
import eu.esa.opt.dataio.flex.compatibility.EarlyProcessorCompatibility;
import eu.esa.opt.dataio.flex.compatibility.FlexProductCompatibility;
import eu.esa.opt.dataio.flex.compatibility.StandardFlexCompatibility;
import eu.esa.opt.dataio.flex.dddb.FlexVariableDescriptor;
import eu.esa.opt.dataio.flex.header.FlexProductHeader;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.dataio.netcdf.util.DataTypeUtils;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FlexReaderUtils {


    public static String mapProductType(String headerProductType) {
        if (headerProductType.contains("L1B") && headerProductType.contains("OBS")) {
            return "FLX_L1B_OBS";
        } else if (headerProductType.contains("L1C") && headerProductType.contains("FLXSYN")) {
            return "FLX_L1C_FLXSYN";
        } else if (headerProductType.contains("L2") && headerProductType.contains("FLXSYN")) {
            return "FLX_L2_FLXSYN";
        }
        throw new IllegalArgumentException("Unknown FLEX product type: " + headerProductType);
    }


    public static Path findHeaderFile(Path inputPath) throws IOException {
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


    public static FlexProductCompatibility detectCompatibility(FlexProductHeader header) {
        for (final String fileName : header.getDataFileNames()) {
            if (fileName.endsWith(".nc.nc")) {
                return new EarlyProcessorCompatibility();
            }
        }
        return new StandardFlexCompatibility();
    }


    public static void setScaleAndOffset(Band band, Variable ncVariable) {
        final Attribute scaleFactor = ncVariable.findAttribute("scale_factor");
        if (scaleFactor != null) {
            final Double scale = getNumericAttributeValue(scaleFactor);
            if (scale != null) {
                band.setScalingFactor(scale);
            }
        }
        final Attribute addOffset = ncVariable.findAttribute("add_offset");
        if (addOffset != null) {
            final Double offset = getNumericAttributeValue(addOffset);
            if (offset != null) {
                band.setScalingOffset(offset);
            }
        }
    }

    public static void setFillValue(Band band, Variable ncVariable) {
        final Attribute fillValue = ncVariable.findAttribute("_FillValue");
        if (fillValue != null) {
            final Double value = getNumericAttributeValue(fillValue);
            if (value != null) {
                band.setNoDataValue(value);
                band.setNoDataValueUsed(true);
            }
        }
    }

    public static void setScaleOffsetAndFillValue(Band band, FlexVariableDescriptor descriptor) {
        if (Double.compare(descriptor.getScaleFactor(), 1.0) != 0) {
            band.setScalingFactor(descriptor.getScaleFactor());
        }
        if (Double.compare(descriptor.getAddOffset(), 0.0) != 0) {
            band.setScalingOffset(descriptor.getAddOffset());
        }
        final Double fillValue = descriptor.getFillValue();
        if (fillValue != null) {
            band.setNoDataValue(fillValue);
            band.setNoDataValueUsed(true);
        }
    }

    private static Double getNumericAttributeValue(Attribute attribute) {
        final Number numericValue = attribute.getNumericValue();
        if (numericValue != null) {
            return numericValue.doubleValue();
        }
        final String stringValue = attribute.getStringValue();
        if (stringValue != null) {
            try {
                return Double.parseDouble(stringValue.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }


    public static void setSpectralWavelength(Band band, Product product, String metaElementName, int elementIndex) {
        final Float value = getSpectralValue(product, metaElementName, elementIndex);
        if (value != null) {
            band.setSpectralWavelength(value);
        }
    }

    public static void setSpectralFwhm(Band band, Product product, String metaElementName, int elementIndex) {
        final Float value = getSpectralValue(product, metaElementName, elementIndex);
        if (value != null) {
            band.setSpectralBandwidth(value);
        }
    }

    private static Float getSpectralValue(Product product, String metaElementName, int elementIndex) {
        if (metaElementName == null || metaElementName.isEmpty() || elementIndex <= 0) {
            return null;
        }

        final MetadataElement netcdfElement = product.getMetadataRoot().getElement(FlexProductReader.NETCDF_BASE_METADATA_ELEMENT);
        if (netcdfElement == null) {
            return null;
        }

        final MetadataElement element = netcdfElement.getElement(metaElementName);
        if (element == null) {
            return null;
        }

        final MetadataAttribute valueAttr = element.getAttribute("value");
        if (valueAttr == null || elementIndex > valueAttr.getData().getNumElems()) {
            return null;
        }

        return (float) valueAttr.getData().getElemDoubleAt(elementIndex - 1);
    }


    public static MetadataElement extractMetadata(Variable variable) throws IOException {
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



    public static void addStringAttribute(MetadataElement element, String name, String value) {
        element.addAttribute(new MetadataAttribute(name, ProductData.createInstance(value), true));
    }
}
