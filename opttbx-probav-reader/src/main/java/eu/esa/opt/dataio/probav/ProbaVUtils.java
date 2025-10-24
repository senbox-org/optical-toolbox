package eu.esa.opt.dataio.probav;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.object.Attribute;
import hdf.object.Datatype;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5Datatype;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.SystemUtils;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.List;
import java.util.logging.Level;

/**
 * Proba-V utility methods
 *
 * @author olafd
 */
public class ProbaVUtils {

    /**
     * Returns the value of a given HDF attribute
     *
     * @param attribute - input attribute
     * @return the value as string
     */
    public static String getAttributeValue(Attribute attribute) throws Exception {
        String result = "";
        switch (attribute.getAttributeDatatype().getDatatypeClass()) {
            case Datatype.CLASS_INTEGER -> {
                if (attribute.getAttributeData().getClass() == long[].class) {
                    long[] ivals = (long[]) attribute.getAttributeData();
                    for (long ival : ivals) {
                        result = result.concat(ival + " ");
                    }
                }
                if (attribute.getAttributeData().getClass() == int[].class) {
                    int[] ivals = (int[]) attribute.getAttributeData();
                    for (int ival : ivals) {
                        result = result.concat(ival + " ");
                    }
                }
                if (attribute.getAttributeData().getClass() == short[].class) {
                    short[] ivals = (short[]) attribute.getAttributeData();
                    for (short ival : ivals) {
                        result = result.concat(ival + " ");
                    }
                }
                if (attribute.getAttributeData().getClass() == byte[].class) {
                    byte[] ivals = (byte[]) attribute.getAttributeData();
                    for (byte ival : ivals) {
                        result = result.concat(ival + " ");
                    }
                }
            }
            case Datatype.CLASS_FLOAT -> {
                if (attribute.getAttributeData().getClass() == float[].class) {
                    float[] fvals = (float[]) attribute.getAttributeData();
                    for (float fval : fvals) {
                        result = result.concat(fval + " ");
                    }
                }
                if (attribute.getAttributeData().getClass() == double[].class) {
                    double[] dvals = (double[]) attribute.getAttributeData();
                    for (double dval : dvals) {
                        result = result.concat(dval + " ");
                    }
                }
            }
            case Datatype.CLASS_STRING -> {
                String[] svals = (String[]) attribute.getAttributeData();
                for (String sval : svals) {
                    result = result.concat(sval + " ");
                }
            }
            default -> {
            }
        }

        return result.trim();
    }

    /**
     * Returns the value of an HDF string attribute with given name
     *
     * @param metadata      - the metadata containing the attributes
     * @param attributeName - the attribute name
     * @return the value as string
     */
    public static String getStringAttributeValue(List<?> metadata, String attributeName) {
        String stringAttr = null;
        for (Object metadataItem : metadata) {
            final Attribute attribute = (Attribute) metadataItem;
            if (attribute.getAttributeName().equals(attributeName)) {
                try {
                    stringAttr = getAttributeValue(attribute);
                } catch (Exception e) {
                    SystemUtils.LOG.log(Level.WARNING, "Cannot parse string attribute: " +
                            e.getMessage());
                }
            }
        }
        return stringAttr;
    }

    /**
     * Returns the value of an HDF double attribute with given name
     *
     * @param metadata      - the metadata containing the attributes
     * @param attributeName - the attribute name
     * @return the value as double
     */
    public static double getDoubleAttributeValue(List<?> metadata, String attributeName) {
        double doubleAttr = Double.NaN;
        for (Object metadataItem : metadata) {
            final Attribute attribute = (Attribute) metadataItem;
            if (attribute.getAttributeName().equals(attributeName)) {
                try {
                    doubleAttr = Double.parseDouble(getAttributeValue(attribute));
                } catch (Exception e) {
                    SystemUtils.LOG.log(Level.WARNING, "Cannot parse float attribute: " + e.getMessage());
                }
            }
        }
        return doubleAttr;
    }

    /**
     * Returns product start/end times extracted from HDF attribute
     *
     * @param metadata- the metadata containing the attributes
     * @return start/end times as String[]
     */
    public static String[] getStartEndTimeFromAttributes(List<?> metadata) throws Exception {
        String startDate = "";
        String startTime = "";
        String endDate = "";
        String endTime = "";
        for (Object metadataItem : metadata) {
            final Attribute attribute = (Attribute) metadataItem;
            switch (attribute.getAttributeName()) {
                case "OBSERVATION_START_DATE" -> startDate = getAttributeValue(attribute);
                case "OBSERVATION_START_TIME" -> startTime = getAttributeValue(attribute);
                case "OBSERVATION_END_DATE" -> endDate = getAttributeValue(attribute);
                case "OBSERVATION_END_TIME" -> endTime = getAttributeValue(attribute);
            }
        }

        if (startDate.isEmpty() || startTime.isEmpty() || endDate.isEmpty() || endTime.isEmpty()) {
            return null;
        }
        // format is 'yyyy-mm-dd hh:mm:ss'
        String[] startStopTimes = new String[2];
        startStopTimes[0] = startDate + " " + startTime;
        startStopTimes[1] = endDate + " " + endTime;
        return startStopTimes;
    }

    /**
     * Reads data from a Proba-V HDF input file into a data buffer
     *
     * @param file_id       - HDF file id
     * @param width         - buffer width
     * @param height        - buffer height
     * @param offsetX       - buffer X offset
     * @param offsetY       - buffer Y offset
     * @param datasetName   - the HDF dataset name
     * @param datatypeClass - the HDF datatype
     * @param destBuffer    - the data buffer being filled
     */
    public static void readProbaVData(long file_id,
                                      int width, int height, long offsetX, long offsetY,
                                      String datasetName, int datatypeClass,
                                      ProductData destBuffer) {
        try {
            final long dataset_id = H5.H5Dopen(file_id,                       // Location identifier
                    datasetName,                   // Dataset name
                    HDF5Constants.H5P_DEFAULT);    // Identifier of dataset access property list

            final long dataspace_id = H5.H5Dget_space(dataset_id);

            final long[] offset = {offsetY, offsetX};
            final long[] count = {height, width};

            H5.H5Sselect_hyperslab(dataspace_id,                   // Identifier of dataspace selection to modify
                    HDF5Constants.H5S_SELECT_SET,   // Operation to perform on current selection.
                    offset,                         // Offset of start of hyperslab
                    null,                           // Hyperslab stride.
                    count,                          // Number of blocks included in hyperslab.
                    null);                          // Size of block in hyperslab.

            final long memspace_id = H5.H5Screate_simple(count.length, // Number of dimensions of dataspace.
                    count,        // An array of the size of each dimension.
                    null);       // An array of the maximum size of each dimension.

            final long[] offset_out = {0L, 0L};
            H5.H5Sselect_hyperslab(memspace_id,                        // Identifier of dataspace selection to modify
                    HDF5Constants.H5S_SELECT_SET,       // Operation to perform on current selection.
                    offset_out,                         // Offset of start of hyperslab
                    null,                               // Hyperslab stride.
                    count,                              // Number of blocks included in hyperslab.
                    null);                          // Size of block in hyperslab.

            long dataType = ProbaVUtils.getAttributeDatatypeForH5Dread(datatypeClass);

            if (destBuffer != null) {
                H5.H5Dread(dataset_id,                    // Identifier of the dataset read from.
                        dataType,                      // Identifier of the memory datatype.
                        memspace_id,                   //  Identifier of the memory dataspace.
                        dataspace_id,                  // Identifier of the dataset's dataspace in the file.
                        HDF5Constants.H5P_DEFAULT,     // Identifier of a transfer property list for this I/O operation.
                        destBuffer.getElems());        // Buffer to store data read from the file.

                H5.H5Dclose(dataset_id);
                H5.H5Sclose(memspace_id);
            }

        } catch (Exception e) {
            SystemUtils.LOG.log(Level.SEVERE, "Cannot read ProbaV raster data '" + datasetName + "': " + e.getMessage());
        }
    }

    /**
     * Checks by data set tree node inspection if a Proba-V product is a Level 3 NDVI product.
     *
     * @param productTypeNode - the data set tree node (starting at LEVEL3)
     * @return boolean
     */
    public static boolean isLevel3Ndvi(Group productTypeNode) {
        boolean hasNdvi = false;
        boolean isNdvi = true;
        for (int i = 0; i < productTypeNode.getNumberOfMembersInFile(); i++) {
            // we have: 'GEOMETRY', 'NDVI', 'QUALITY', 'RADIOMETRY', 'TIME'
            final Group productTypeChildNode = (Group) productTypeNode.getMember(i);
            final String productTypeChildNodeName = productTypeChildNode.getName();

            if (productTypeChildNodeName.equals("NDVI")) {
                hasNdvi = true;
            } else {
                if (productTypeChildNode.getNumberOfMembersInFile() > 0) {
                    isNdvi = false;
                }
            }
            if (!isNdvi && hasNdvi) {
                return false;
            }
        }
        return hasNdvi;
    }

    /**
     * Checks by data set tree node inspection if a Proba-V product is a Level 3 TOC product.
     *
     * @param productTypeNode - the data set tree node (starting at LEVEL3)
     * @return boolean
     */
    public static boolean isLevel3Toc(Group productTypeNode) {
        return isReflectanceType(productTypeNode);
    }

    /**
     * Checks if tree child note corresponds to viewing angle group
     *
     * @param geometryChildNodeName - the tree child note
     * @return boolean
     */
    public static boolean isProbaVViewAngleGroupNode(String geometryChildNodeName) {
        return geometryChildNodeName.equals("SWIR") || geometryChildNodeName.equals("VNIR");
    }

    /**
     * Checks if tree child note corresponds to sun angle group
     *
     * @param geometryChildNodeName - the tree child note
     * @return boolean
     */
    public static boolean isProbaVSunAngleDataNode(String geometryChildNodeName) {
        return geometryChildNodeName.equals("SAA") || geometryChildNodeName.equals("SZA");
    }

    /**
     * Creates a target band matching given metadata information
     *
     * @param product  - the target product
     * @param bandDS   - the HDF dataset
     * @param bandName - band name
     * @return the target band
     */
    public static Band createTargetBand(Product product, H5ScalarDS bandDS, String bandName) throws Exception {
        final List<Attribute> metadata = bandDS.getMetadata();
        final double scaleFactorAttr = ProbaVUtils.getDoubleAttributeValue(metadata, "SCALE");
        final double scaleFactor = Double.isNaN(scaleFactorAttr) ? 1.0f : scaleFactorAttr;
        final double scaleOffsetAttr = ProbaVUtils.getDoubleAttributeValue(metadata, "OFFSET");
        final double scaleOffset = Double.isNaN(scaleOffsetAttr) ? 0.0f : scaleOffsetAttr;
        final int dataType = extractDataTypeOfProductData(bandDS);
        final Band band = product.addBand(bandName, dataType);
        band.setScalingFactor(1.0 / scaleFactor);
        band.setScalingOffset(-1.0 * scaleOffset / scaleFactor);

        return band;
    }

    /**
     * Provides a HDF5 scalar dataset corresponding to given HDF product node
     *
     * @param level3BandsChildNode - the data node
     * @return - the data set (H5ScalarDS)
     */
    public static H5ScalarDS getH5ScalarDS(HObject level3BandsChildNode) {
        H5ScalarDS scalarDS = (H5ScalarDS) level3BandsChildNode;
        scalarDS.init();
        return scalarDS;
    }

    /**
     * Extracts a HDF metadata element and adds accordingly to given product
     *
     * @param metadataAttributes  - the HDF metadata attributes
     * @param parentElement       - the parent metadata element
     * @param metadataElementName - the element name
     */
    public static void addMetadataElementWithAttributes(List<?> metadataAttributes,
                                                        final MetadataElement parentElement,
                                                        String metadataElementName) throws Exception {
        final MetadataElement metadataElement = new MetadataElement(metadataElementName);
        for (Object metadataAttributesItem : metadataAttributes) {
            final Attribute attribute = (Attribute) metadataAttributesItem;
            metadataElement.addAttribute(new MetadataAttribute(attribute.getAttributeName(),
                    ProductData.createInstance(ProbaVUtils.getAttributeValue(attribute)), true));
        }
        parentElement.addElement(metadataElement);
    }

    /**
     * Adds HDF metadata attributes to a given metadata element
     *
     * @param metadataAttributes - the HDF metadata attributes
     * @param parentElement      - the parent metadata element
     */
    public static void addMetadataAttributes(List<?> metadataAttributes,
                                             final MetadataElement parentElement) throws Exception {
        for (Object metadataAttributesItem : metadataAttributes) {
            final Attribute attribute = (Attribute) metadataAttributesItem;
            parentElement.addAttribute(new MetadataAttribute(attribute.getAttributeName(),
                    ProductData.createInstance(ProbaVUtils.getAttributeValue(attribute)), true));
        }
    }

    /**
     * Extracs start/stop times from HDF metadata and adds to given product
     *
     * @param product  - the product
     * @param timeNode - the HDF node containing the time information
     * @throws Exception - when an error occurs
     */
    public static void addStartStopTimes(Product product, HObject timeNode) throws Exception  {
        final H5Group timeGroup = (H5Group) timeNode;
        final List<?> timeMetadata = timeGroup.getMetadata();
        final String[] startEndTime = ProbaVUtils.getStartEndTimeFromAttributes(timeMetadata);
        if (startEndTime != null) {
            product.setStartTime(ProductData.UTC.parse(startEndTime[0],
                    ProbaVConstants.PROBAV_DATE_FORMAT_PATTERN));
            product.setEndTime(ProductData.UTC.parse(startEndTime[1],
                    ProbaVConstants.PROBAV_DATE_FORMAT_PATTERN));
        }
    }

    /**
     * Adds metadata which corresponds to a band subgroup (NDVI, QUALITY or TIME)
     *
     * @param product       - the product
     * @param parentNode    - the HDF parent node
     * @param bandGroupName - band subgroup name (should be NDVI, QUALITY or TIME)
     * @throws Exception    - When an error occurs
     */
    public static void addBandSubGroupMetadata(Product product, Group parentNode, String bandGroupName) throws Exception {
        // NDVI, QUALITY, TIME
        addRootMetadataElement(product, parentNode, bandGroupName);

        final MetadataElement rootMetadataElement = product.getMetadataRoot().getElement(bandGroupName);

        final HObject childNode = parentNode.getMember(0);

        if (!bandGroupName.equals(ProbaVConstants.QUALITY_BAND_GROUP_NAME)) {
            // skip the 'SM' metadata as it is not the original SM band
            final H5ScalarDS ds = ProbaVUtils.getH5ScalarDS(childNode);
            final List<?> childMetadata = ds.getMetadata();
            ProbaVUtils.addMetadataAttributes(childMetadata, rootMetadataElement);
        }
    }

    /**
     * Adds a metadata element with attributes to root node
     *
     * @param product     - the product
     * @param parentNode  - the HDF parent node
     * @param elementName - the metadata element name
     * @throws HDF5Exception - when an error occurs
     */
    public static void addRootMetadataElement(Product product, HObject parentNode, String elementName)
            throws Exception {
        final H5Group parentGeometryGroup = (H5Group) parentNode;
        final List<?> parentGeometryMetadata = parentGeometryGroup.getMetadata();
        ProbaVUtils.addMetadataElementWithAttributes(parentGeometryMetadata, product.getMetadataRoot(), elementName);
    }

    /**
     * Extracts unit and description from HDF metadata and adds to given band
     *
     * @param metadata - HDF metadata
     * @param band     - the band
     */
    public static void setBandUnitAndDescription(List<Attribute> metadata, Band band) {
        band.setDescription(ProbaVUtils.getStringAttributeValue(metadata, "DESCRIPTION"));
        band.setUnit(ProbaVUtils.getStringAttributeValue(metadata, "UNITS"));
    }

    /**
     * Sets Proba-V spectral band properties
     *
     * @param treeNode - node to extract metadata from
     * @param band     - the spectral band
     */
    public static void setSpectralBandProperties(HObject treeNode, Band band) throws HDF5Exception {
        final H5Group group = (H5Group) treeNode;
        final List<?> metadata = group.getMetadata();
        final double solarIrradiance = ProbaVUtils.getDoubleAttributeValue(metadata, "SOLAR_IRRADIANCE");
        band.setSolarFlux((float) solarIrradiance);
        if (band.getName().endsWith("REFL_BLUE")) {
            band.setSpectralBandIndex(0);
            band.setSpectralWavelength(462.0f);
            band.setSpectralBandwidth(48.0f);
        } else if (band.getName().endsWith("REFL_RED")) {
            band.setSpectralBandIndex(1);
            band.setSpectralWavelength(655.5f);
            band.setSpectralBandwidth(81.0f);
        } else if (band.getName().endsWith("REFL_NIR")) {
            band.setSpectralBandIndex(2);
            band.setSpectralWavelength(843.0f);
            band.setSpectralBandwidth(142.0f);
        } else if (band.getName().endsWith("REFL_SWIR")) {
            band.setSpectralBandIndex(3);
            band.setSpectralWavelength(1599.0f);
            band.setSpectralBandwidth(70.0f);
        }
    }

    /**
     * Sets the Proba-V geo coding to a product as extracted from HDF metadata information
     *
     * @param product              - the product
     * @param inputFileRootNode    - HDF root tree node
     * @param productTypeChildNode - the product type child node (LEVEL2A or LEVEL3)
     * @param productWidth         - product width
     * @param productHeight        - product height
     * @throws HDF5Exception       - when an error occurs
     */
    public static void setProbaVGeoCoding(Product product, HObject inputFileRootNode, HObject productTypeChildNode,
                                          int productWidth, int productHeight) throws HDF5Exception {

        final H5Group h5GeometryGroup = (H5Group) productTypeChildNode;
        final List<?> geometryMetadata = h5GeometryGroup.getMetadata();
        final double easting = ProbaVUtils.getDoubleAttributeValue(geometryMetadata, "TOP_LEFT_LONGITUDE");
        final double northing = ProbaVUtils.getDoubleAttributeValue(geometryMetadata, "TOP_LEFT_LATITUDE");
        // pixel size: 10deg/rasterDim, it is also in the 6th and 7th value of MAPPING attribute in the raster nodes
        final double topRightLon = ProbaVUtils.getDoubleAttributeValue(geometryMetadata, "TOP_RIGHT_LONGITUDE");
        final double pixelSizeX = Math.abs(topRightLon - easting) / (productWidth - 1);
        final double bottomLeftLat = ProbaVUtils.getDoubleAttributeValue(geometryMetadata, "BOTTOM_LEFT_LATITUDE");
        final double pixelSizeY = (northing - bottomLeftLat) / (productHeight - 1);

        final H5Group h5RootGroup = (H5Group) inputFileRootNode;
        final List<?> rootMetadata = h5RootGroup.getMetadata();
        final String crsString = ProbaVUtils.getStringAttributeValue(rootMetadata, "MAP_PROJECTION_WKT");
        try {
            final CoordinateReferenceSystem crs = CRS.parseWKT(crsString);
            final CrsGeoCoding geoCoding = new CrsGeoCoding(crs, productWidth, productHeight, easting, northing, pixelSizeX, pixelSizeY);
            product.setSceneGeoCoding(geoCoding);
        } catch (Exception e) {
            SystemUtils.LOG.log(Level.WARNING, "Cannot attach geocoding: " + e.getMessage());
        }
    }

    /**
     * Provides a ProductData instance according to given HDF5 data type
     *
     * @param datatypeClass - the HDF5 data type
     * @param width         - buffer width
     * @param height        - buffer height
     * @return the data buffer
     */
    public static ProductData getDataBufferForH5Dread(int datatypeClass, int width, int height) {
        switch (datatypeClass) {
            case H5Datatype.CLASS_CHAR -> {
                return ProductData.createInstance(new byte[width * height]);
            }
            case H5Datatype.CLASS_FLOAT -> {
                return ProductData.createInstance(new float[width * height]);
            }
            case H5Datatype.CLASS_INTEGER -> {
                return ProductData.createInstance(new short[width * height]);
            }
            default -> {
            }
        }
        return null;
    }

    //// private methods ////

    private static boolean isReflectanceType(Group productTypeNode) {
        for (int i = 0; i < productTypeNode.getNumberOfMembersInFile(); i++) {
            // we have: 'GEOMETRY', 'NDVI', 'QUALITY', 'RADIOMETRY', 'TIME'
            final Group productTypeChildNode = (Group) productTypeNode.getMember(i);
            final String productTypeChildNodeName = productTypeChildNode.getName();

            if (productTypeChildNodeName.equals("RADIOMETRY")) {
                // children are BLUE, RED, NIR, SWIR
                final Group radiometryChildNode = (Group) productTypeChildNode.getMember(0);
                return radiometryChildNode.getMember(0).getName().equals("TOC");
            }
        }
        return false;
    }

    private static long getAttributeDatatypeForH5Dread(int datatypeClass) {
        switch (datatypeClass) {
            case H5Datatype.CLASS_BITFIELD, H5Datatype.CLASS_CHAR -> {
                return HDF5Constants.H5T_NATIVE_UINT8;
            }
            case H5Datatype.CLASS_FLOAT -> {
                return HDF5Constants.H5T_NATIVE_FLOAT;
            }
            case H5Datatype.CLASS_INTEGER -> {
                return HDF5Constants.H5T_NATIVE_INT16;
            }
            default -> {
            }
        }
        return -1;
    }

    /**
     * Extracts the SNAP band object data type from a Prova-V product file HDF5 scalar object
     * @param h5ScalarDS the Prova-V product file HDF5 scalar object
     * @return the SNAP band object data type from a Prova-V product file
     * @throws HDF5Exception if an error occurs
     */
    private static int extractDataTypeOfProductData(H5ScalarDS h5ScalarDS) throws Exception {
        final Datatype dt = h5ScalarDS.getDatatype();
        final long size = dt.getDatatypeSize();

        switch (dt.getDatatypeClass()) {
            case Datatype.CLASS_INTEGER:
                if (size == 8) {
                    return ProductData.TYPE_INT64;
                }
                if (size == 4) {
                    return ProductData.TYPE_INT32;
                }
                if (size == 2) {
                    return ProductData.TYPE_INT16;
                }
                if (size == 1) {
                    return ProductData.TYPE_INT8;
                }
                break;
            case Datatype.CLASS_FLOAT:
                if (size == 4) {
                    return ProductData.TYPE_FLOAT32;
                }
                if (size == 8) {
                    return ProductData.TYPE_FLOAT64;
                }
                break;
            case Datatype.CLASS_CHAR:
            case Datatype.CLASS_STRING:
                return ProductData.TYPE_INT8;
            default:
                break;
        }
        return ProductData.TYPE_UNDEFINED;
    }
}
