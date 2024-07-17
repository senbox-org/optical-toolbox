package eu.esa.opt.dataio.prisma;

import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.geotools.xml.xsi.XSISimpleTypes;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.VariableDS;

import java.io.IOException;
import java.util.List;

public class MetadataReader {

    public static void readMetadata(NetcdfFile _hdfFile, Product product) throws IOException {
        final MetadataElement root = product.getMetadataRoot();
        final MetadataElement globalAttributes = new MetadataElement("GlobalAttributes");
        root.addElement(globalAttributes);
        final List<Attribute> attributes = _hdfFile.getGlobalAttributes();
        addAttributesTo(globalAttributes, attributes);
        final Group group = _hdfFile.getRootGroup();
        addAttributeTreeTo(root, group);
        addAuxVariableTreeTo(root, group, false);
    }

    static void addAttributeTreeTo(MetadataElement element, Group group) throws IllegalFileFormatException {
        final List<Group> groups = group.getGroups();
        for (Group childGroup : groups) {
            final String childName = childGroup.getName();
            final MetadataElement nestedElement = new MetadataElement(childName);
            final List<Attribute> attributes = childGroup.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                addAttributesTo(nestedElement, attributes);
            }
            addAttributeTreeTo(nestedElement, childGroup);
            if (nestedElement.getNumAttributes() > 0 || nestedElement.getNumElements() > 0) {
                element.addElement(nestedElement);
            }
        }
    }

    static void addAuxVariableTreeTo(MetadataElement element, Group group, boolean isHdfeosPath) throws IOException {
        final List<Group> groups = group.getGroups();
        for (Group childGroup : groups) {
            final String childName = childGroup.getShortName();
            final boolean alreadyContainsElement = element.containsElement(childName);
            final MetadataElement nestedElement;
            if (alreadyContainsElement) {
                nestedElement = element.getElement(childName);
            } else {
                nestedElement = new MetadataElement(childName);
            }
            final List<Variable> variables = childGroup.getVariables();
            if (variables != null && !variables.isEmpty()) {
                for (Variable variable : variables) {
                    final int rank = variable.getRank();
                    if (!isHdfeosPath || rank == 1 || (rank == 2 && variable.getShape()[1] ==1)) {
                        handleArrayDataOfVariable(nestedElement, variable);
                    }
                }
            }
            addAuxVariableTreeTo(nestedElement, childGroup, isHdfeosPath || "HDFEOS".equals(childName));
            if (!alreadyContainsElement) {
                if (nestedElement.getNumAttributes() > 0 || nestedElement.getNumElements() > 0) {
                    element.addElement(nestedElement);
                }
            }
        }
    }

    static void addAttributesTo(MetadataElement element, List<Attribute> attributes) throws IllegalFileFormatException {
        for (Attribute att : attributes) {
            handleAttributeData(element, att);
        }
    }

    static void handleAttributeData(MetadataElement element, Attribute att) throws IllegalFileFormatException {
        final String attName = att.getShortName();
        if (att.isArray()) {
            handleArrayDataOfAttributes(element, attName, att);
        } else {
            handleScalarData(element, attName, att);
        }
    }

//    static void handleVariableData(MetadataElement element, Variable variable) throws IOException {
//        final String varName = variable.getShortName();
//        final Array array = variable.read();
//        final Array reduced = array.reduce();
//        variable.
//        if (variable.redisArray()) {
//            handleArrayData(element, varName, variable);
//        } else {
//            handleScalarData(element, varName, variable);
//        }
//    }

    static void handleScalarData(MetadataElement element, String attName, Attribute att) throws IllegalFileFormatException {
        ProductData pd = createProductDataInstanceForScalarData(att);
        if (pd != null) {
            element.addAttribute(new MetadataAttribute(attName, pd, true));
        }
    }

    static ProductData createProductDataInstanceForScalarData(Attribute att) throws IllegalFileFormatException {
        if (att.isString()) {
            return ProductData.createInstance(att.getStringValue());
        }
        final DataType dataType = att.getDataType();
        if (!dataType.isNumeric()) {
            throw new IllegalFileFormatException("Unsupported data type: " + dataType);
        }
        final boolean unsigned = dataType.isUnsigned();
        final Number number = att.getNumericValue();
        final ProductData pd;
        final String numberSimpleName = number.getClass().getSimpleName();
        if (numberSimpleName.equals("Byte")) {
            if (unsigned) {
                pd = ProductData.createInstance(ProductData.TYPE_UINT8, 1);
            } else {
                pd = ProductData.createInstance(ProductData.TYPE_INT8, 1);
            }
            pd.setElemLong(number.longValue());
        } else if (numberSimpleName.equals("Short")) {
            if (unsigned) {
                pd = ProductData.createInstance(ProductData.TYPE_UINT16, 1);
            } else {
                pd = ProductData.createInstance(ProductData.TYPE_INT16, 1);
            }
            pd.setElemLong(number.longValue());
        } else if (numberSimpleName.equals("Integer")) {
            if (unsigned) {
                pd = ProductData.createInstance(ProductData.TYPE_UINT32, 1);
            } else {
                pd = ProductData.createInstance(ProductData.TYPE_INT32, 1);
            }
            pd.setElemLong(number.longValue());
        } else if (numberSimpleName.equals("Long")) {
            if (unsigned) {
                throw new IllegalFileFormatException("Unsupported data type: " + dataType);
            } else {
                pd = ProductData.createInstance(ProductData.TYPE_INT64, 1);
            }
            pd.setElemLong(number.longValue());
        } else if (numberSimpleName.equals("Float")) {
            pd = ProductData.createInstance(ProductData.TYPE_FLOAT32, 1);
            pd.setElemFloat(number.floatValue());
        } else if (numberSimpleName.equals("Double")) {
            pd = ProductData.createInstance(ProductData.TYPE_FLOAT64, 1);
            pd.setElemDouble(number.doubleValue());
        } else {
            throw new IllegalFileFormatException("Unsupported data type: " + dataType);
        }
        return pd;
    }

    public static void handleArrayDataOfVariable(MetadataElement element, Variable var) throws IOException {
        final String varName = var.getShortName();
        final MetadataElement varElement = new MetadataElement(varName);
        final DataType dataType = var.getDataType();
        final boolean unsigned = dataType.isUnsigned();
        final Object data = var.read().reduce().copyToNDJavaArray();
        switch (dataType) {
            case BYTE:
            case UBYTE:
                addMetaAttributesFromByte(varElement, varName, data, unsigned);
                break;
            case SHORT:
            case USHORT:
                addMetaAttributesFromShort(varElement, varName, data, unsigned);
                break;
            case INT:
            case UINT:
                addMetaAttributesFromInt(varElement, varName, data, unsigned);
                break;
            case LONG:
                addMetaAttributesFromLong(varElement, varName, data, unsigned);
                break;
            case FLOAT:
                addMetaAttributesFromFloat(varElement, varName, data);
                break;
            case DOUBLE:
                addMetaAttributesFromDouble(varElement, varName, data);
                break;
            case CHAR:
                final String entireString = new String((char[]) data);
                ProductData stringData = ProductData.createInstance(entireString);
                varElement.addAttribute(new MetadataAttribute(varName + "_entire_string", stringData, true));

                final String replaced = entireString.replaceAll("\\t", "    ");
                final String[] lines = replaced.split("(\\r\\n|\\n\\r|\\n|\\r)");
                addMetaAttributesFromString(varElement, varName, lines);
                break;
            default:
                throw new IllegalFileFormatException("Unsupported data type: " + dataType);
        }
        if (varElement.getNumElements() > 0 || varElement.getNumAttributes() > 0) {
            element.addElement(varElement);
        }
    }

    static void handleArrayDataOfAttributes(MetadataElement element, String attName, Attribute att) throws IllegalFileFormatException {
        final MetadataElement attElement = new MetadataElement(attName);
        final DataType dataType = att.getDataType();
        final Class<?> javaType = dataType.getPrimitiveClassType();
        final Array values = att.getValues();
        final Object data = values.copyToNDJavaArray();
        final boolean unsigned = values.isUnsigned();
        switch (javaType.getSimpleName()) {
            case "byte":
                addMetaAttributesFromByte(attElement, attName, data, unsigned);
                break;
            case "short":
                addMetaAttributesFromShort(attElement, attName, data, unsigned);
                break;
            case "int":
                addMetaAttributesFromInt(attElement, attName, data, unsigned);
                break;
            case "long":
                addMetaAttributesFromLong(attElement, attName, data, unsigned);
                break;
            case "float":
                addMetaAttributesFromFloat(attElement, attName, data);
                break;
            case "String":
                final String[] strings = new String[att.getLength()];
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = att.getStringValue(i);
                }
                addMetaAttributesFromString(attElement, attName, strings);
                break;
            default:
                throw new IllegalFileFormatException("Unsupported data type: " + dataType);
        }
        if (attElement.getNumElements() > 0 || attElement.getNumAttributes() > 0) {
            element.addElement(attElement);
        }
    }

    static void addMetaAttributesFromByte(MetadataElement attElement, String attName, Object data, boolean unsigned) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            ProductData pd;
            if (unsigned) {
                pd = ProductData.createUnsignedInstance((byte[]) data);
            } else {
                pd = ProductData.createInstance((byte[]) data);
            }
            attElement.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            byte[][] bytes = (byte[][]) data;
            for (int i = 0; i < bytes.length; i++) {
                final ProductData pd;
                if (unsigned) {
                    pd = ProductData.createUnsignedInstance(bytes[i]);
                } else {
                    pd = ProductData.createInstance(bytes[i]);
                }
                String subName = String.format("%s.%d", attName, i + 1);
                attElement.addAttribute(new MetadataAttribute(subName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromShort(MetadataElement attElement, String attName, Object data, boolean unsigned) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            ProductData pd;
            if (unsigned) {
                pd = ProductData.createUnsignedInstance((short[]) data);
            } else {
                pd = ProductData.createInstance((short[]) data);
            }
            attElement.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            short[][] shorts = (short[][]) data;
            for (int i = 0; i < shorts.length; i++) {
                final ProductData pd;
                if (unsigned) {
                    pd = ProductData.createUnsignedInstance(shorts[i]);
                } else {
                    pd = ProductData.createInstance(shorts[i]);
                }
                String subName = String.format("%s.%d", attName, i + 1);
                attElement.addAttribute(new MetadataAttribute(subName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromInt(MetadataElement attElement, String attName, Object data, boolean unsigned) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            ProductData pd;
            if (unsigned) {
                pd = ProductData.createUnsignedInstance((int[]) data);
            } else {
                pd = ProductData.createInstance((int[]) data);
            }
            attElement.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            int[][] ints = (int[][]) data;
            for (int i = 0; i < ints.length; i++) {
                final ProductData pd;
                if (unsigned) {
                    pd = ProductData.createUnsignedInstance(ints[i]);
                } else {
                    pd = ProductData.createInstance(ints[i]);
                }
                String subName = String.format("%s.%d", attName, i + 1);
                attElement.addAttribute(new MetadataAttribute(subName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromLong(MetadataElement arrayAttElem, String attName, Object data, boolean unsigned) throws IllegalFileFormatException {
        if (unsigned) {
            throw new IllegalFileFormatException("Unsigned long is not supported for attribute: '" + attName + "'.");
        }
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            final ProductData pd = ProductData.createInstance((long[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            long[][] longs = (long[][]) data;
            for (int i = 0; i < longs.length; i++) {
                final ProductData pd = ProductData.createInstance(longs[i]);
                String subName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(subName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromFloat(MetadataElement arrayAttElem, String attName, Object data) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            final ProductData pd = ProductData.createInstance((float[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            float[][] floats = (float[][]) data;
            for (int i = 0; i < floats.length; i++) {
                final ProductData pd = ProductData.createInstance(floats[i]);
                String metaName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(metaName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromDouble(MetadataElement arrayAttElem, String attName, Object data) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            final ProductData pd = ProductData.createInstance((double[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            double[][] floats = (double[][]) data;
            for (int i = 0; i < floats.length; i++) {
                final ProductData pd = ProductData.createInstance(floats[i]);
                String metaName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(metaName, pd, true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void addMetaAttributesFromString(MetadataElement arrayAttElem, String attName, String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            ProductData stringData = ProductData.createInstance(string);
            final String subName = String.format("%s.%d", attName, i + 1);
            arrayAttElem.addAttribute(new MetadataAttribute(subName, stringData, true));
        }
    }

    static int getNumArrayDimensions(Object array) {
        int dimensions = 0;
        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            dimensions++;
            arrayClass = arrayClass.getComponentType();
        }
        return dimensions;
    }

    public static Class<?> getPrimitiveType(Object array) {
        Class<?> type = array.getClass();
        while (type.isArray()) {
            type = type.getComponentType();
        }
        return type;
    }
}
