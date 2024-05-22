package eu.esa.opt.dataio.prisma;

import io.jhdf.HdfFile;
import io.jhdf.api.Attribute;
import io.jhdf.api.Group;
import io.jhdf.api.Node;
import org.esa.snap.core.dataio.IllegalFileFormatException;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

import java.util.Map;

public class MetadataReader {

    public static void readMetadata(HdfFile _hdfFile, Product product) throws IllegalFileFormatException {
        final MetadataElement root = product.getMetadataRoot();
        final MetadataElement globalAttributes = new MetadataElement("GlobalAttributes");
        root.addElement(globalAttributes);
        final Map<String, Attribute> attributes = _hdfFile.getAttributes();
        addAttributesTo(globalAttributes, attributes);
        final Map<String, Node> children = _hdfFile.getChildren();
        addAttributeTreeTo(root, children);
    }

    static void addAttributeTreeTo(MetadataElement element, Map<String, Node> children) throws IllegalFileFormatException {
        for (Node child : children.values()) {
            final String childName = child.getName();
            final MetadataElement nestedElement = new MetadataElement(childName);
            final Map<String, Attribute> attributes = child.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                addAttributesTo(nestedElement, attributes);
            }
            if (child instanceof Group) {
                addAttributeTreeTo(nestedElement, ((Group) child).getChildren());
            }
            if (nestedElement.getNumAttributes() > 0 || nestedElement.getNumElements() > 0) {
                element.addElement(nestedElement);
            }
        }
    }

    static void addAttributesTo(MetadataElement element, Map<String, Attribute> attributes) throws IllegalFileFormatException {
        for (Attribute att : attributes.values()) {
            handleAttributeData(element, att);
        }
    }

    static void handleAttributeData(MetadataElement element, Attribute att) throws IllegalFileFormatException {
        final String attName = att.getName();
        final Object data = att.getData();
        if (att.isScalar()) {
            handleScalarData(element, attName, data);
        } else {
            handleArrayData(element, attName, att);
        }
    }

    static void handleScalarData(MetadataElement element, String attName, Object data) throws IllegalFileFormatException {
        ProductData pd = createProductDataInstanceForScalarData(data);
        if (pd != null) {
            element.addAttribute(new MetadataAttribute(attName, pd, true));
        }
    }

    static ProductData createProductDataInstanceForScalarData(Object data) throws IllegalFileFormatException {
        ProductData pd;
        switch (data.getClass().getSimpleName()) {
            case "Short":
                pd = ProductData.createInstance(ProductData.TYPE_INT16, 1);
                pd.setElemInt((Short) data);
                break;
            case "Integer":
                pd = ProductData.createInstance(ProductData.TYPE_INT32, 1);
                pd.setElemInt((Integer) data);
                break;
            case "Long":
                pd = ProductData.createInstance(ProductData.TYPE_INT64, 1);
                pd.setElemLong((Long) data);
                break;
            case "Float":
                pd = ProductData.createInstance(ProductData.TYPE_FLOAT32, 1);
                pd.setElemFloat((Float) data);
                break;
            case "String":
                pd = ProductData.createInstance((String) data);
                break;
            default:
                throw new IllegalFileFormatException("Unsupported data type: " + data.getClass().getCanonicalName());
        }
        return pd;
    }

    static void handleArrayData(MetadataElement element, String attName, Attribute att) throws IllegalFileFormatException {
        final MetadataElement arrayAttElem = new MetadataElement(attName);
        final Class<?> javaType = att.getJavaType();
        final Object data = att.getData();
        switch (javaType.getSimpleName()) {
            case "int":
                createArrayDataFromInt(arrayAttElem, attName, data);
                break;
            case "long":
                createArrayDataFromLong(arrayAttElem, attName, data);
                break;
            case "float":
                createArrayDataFromFloat(arrayAttElem, attName, data);
                break;
            case "String":
                createArrayDataFromString(arrayAttElem, attName, data);
                break;
            default:
                throw new IllegalFileFormatException("Unsupported data type: " + data.getClass().getCanonicalName());
        }
        if (arrayAttElem.getNumAttributes() > 0) {
            element.addElement(arrayAttElem);
        }
    }

    static void createArrayDataFromInt(MetadataElement arrayAttElem, String attName, Object data) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            ProductData pd = ProductData.createInstance((int[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            int[][] ints = (int[][]) data;
            for (int i = 0; i < ints.length; i++) {
                String metaName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(metaName, ProductData.createInstance(ints[i]), true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void createArrayDataFromLong(MetadataElement arrayAttElem, String attName, Object data) throws IllegalFileFormatException {
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            final ProductData pd = ProductData.createInstance((long[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            long[][] longs = (long[][]) data;
            for (int i = 0; i < longs.length; i++) {
                String metaName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(metaName, ProductData.createInstance(longs[i]), true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void createArrayDataFromFloat(MetadataElement arrayAttElem, String attName, Object data) throws IllegalFileFormatException {
        ProductData pd;
        final int rank = getNumArrayDimensions(data);
        if (rank == 1) {
            pd = ProductData.createInstance((float[]) data);
            arrayAttElem.addAttribute(new MetadataAttribute(attName, pd, true));
        } else if (rank == 2) {
            float[][] floats = (float[][]) data;
            for (int i = 0; i < floats.length; i++) {
                String metaName = String.format("%s.%d", attName, i + 1);
                arrayAttElem.addAttribute(new MetadataAttribute(metaName, ProductData.createInstance(floats[i]), true));
            }
        } else {
            throw new IllegalFileFormatException("Unsupported number of dimensions: " + rank);
        }
    }

    static void createArrayDataFromString(MetadataElement arrayAttElem, String attName, Object data) {
        String[] strings = (String[]) data;
        for (int i = 0; i < strings.length; i++) {
            String string = strings[i];
            ProductData stringData = ProductData.createInstance(string);
            arrayAttElem.addAttribute(new MetadataAttribute(attName + "." + (i + 1), stringData, true));
        }
    }

    public static int getNumArrayDimensions(Object array) {
        int dimensions = 0;
        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            dimensions++;
            arrayClass = arrayClass.getComponentType();
        }
        return dimensions;
    }
}
