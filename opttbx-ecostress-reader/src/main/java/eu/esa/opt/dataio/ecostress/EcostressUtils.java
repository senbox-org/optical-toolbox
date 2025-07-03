package eu.esa.opt.dataio.ecostress;

import hdf.hdf5lib.exceptions.HDF5Exception;
import hdf.object.Attribute;
import hdf.object.Datatype;
import hdf.object.HObject;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.util.ArrayUtils;

import java.awt.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Ecostress utility methods
 *
 * @author adraghici
 */
public class EcostressUtils {

    private static final String BAND_ATTRIBUTE_NAME_DESCRIPTION = "Description";
    private static final String BAND_ATTRIBUTE_NAME_UNIT = "Units";
    private static final String BAND_ATTRIBUTE_NAME_FILL_VALUE = "_FillValue";

    private static final Logger logger = Logger.getLogger(EcostressUtils.class.getName());

    /**
     * Checks whether a node exists in ECOSTRESS product file
     *
     * @param ecostressFile the ECOSTRESS product file
     * @param nodePaths     the nodes paths to check
     * @return {@code true} when a node exists in ECOSTRESS product file
     */
    public static boolean ecostressNodesExists(EcostressFile ecostressFile, String... nodePaths) {
        for (String nodePath : nodePaths) {
            if (getEcostressNode(ecostressFile, nodePath) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts a metadata element from an ECOSTRESS product file
     *
     * @param ecostressFile         the ECOSTRESS product file
     * @param metadataElementsPaths the metadata element path in ECOSTRESS product file
     */
    public static List<MetadataElement> extractMetadataElements(EcostressFile ecostressFile, String... metadataElementsPaths) {
        final List<MetadataElement> metadataElementsList = new ArrayList<>();
        try (ecostressFile) {
            for (String metadataElementPath : metadataElementsPaths) {
                final H5Group ecostressMetadataGroup = getEcostressH5Group(ecostressFile, metadataElementPath);
                if (ecostressMetadataGroup != null) {
                    final MetadataElement metadataElement = new MetadataElement(ecostressMetadataGroup.getName());
                    for (HObject metadataItemObject : ecostressMetadataGroup.getMemberList()) {
                        final H5ScalarDS metadataItem = (H5ScalarDS) metadataItemObject;
                        try {
                            final ProductData metadataItemValue = getEcostressH5ScalarDSValue(metadataItem);
                            metadataElement.addAttribute(new MetadataAttribute(metadataItemObject.getName(), metadataItemValue, true));
                        } catch (HDF5Exception e) {
                            logger.severe("Fail to extract metadata element from ECOSTRESS product '" + ecostressFile.getName() + "'. Reason: " + e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                    metadataElementsList.add(metadataElement);
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to extract the metadata element from ECOSTRESS product '" + ecostressFile.getName() + "'. Reason: " + e.getMessage());
            throw new IllegalStateException(e);
        }
        return metadataElementsList;
    }

    /**
     * Extracts the start time from an ECOSTRESS product file
     *
     * @param ecostressFile the ECOSTRESS product file
     * @return the start time from an ECOSTRESS product file
     */
    public static ProductData.UTC extractStartTime(EcostressFile ecostressFile) {
        return extractDateTime(ecostressFile, EcostressConstants.ECOSTRESS_STANDARD_METADATA_RANGE_BEGINNING_DATE, EcostressConstants.ECOSTRESS_STANDARD_METADATA_RANGE_BEGINNING_TIME);
    }

    /**
     * Extracts the end time from an ECOSTRESS product file
     *
     * @param ecostressFile the ECOSTRESS product file
     * @return the end time from an ECOSTRESS product file
     */
    public static ProductData.UTC extractEndTime(EcostressFile ecostressFile) {
        return extractDateTime(ecostressFile, EcostressConstants.ECOSTRESS_STANDARD_METADATA_RANGE_ENDING_DATE, EcostressConstants.ECOSTRESS_STANDARD_METADATA_RANGE_ENDING_TIME);
    }

    /**
     * Extracts the bands from an ECOSTRESS product file
     *
     * @param ecostressFile   the ECOSTRESS product file
     * @param bandsGroupNames the bands group paths in the ECOSTRESS product file
     * @return the ECOSTRESS product bands list
     */
    public synchronized static List<Band> extractBandsObjects(EcostressFile ecostressFile, String... bandsGroupNames) {
        final List<Band> bandsList = new ArrayList<>();
        try (ecostressFile) {
            for (String bandsGroupName : bandsGroupNames) {
                final H5Group ecostressBandsGroup = getEcostressH5Group(ecostressFile, bandsGroupName);
                if (ecostressBandsGroup != null) {
                    for (final HObject bandElementObject : ecostressBandsGroup.getMemberList()) {
                        if (bandElementObject instanceof H5ScalarDS bandElement) {
                            final String bandName = getBandNameFromEcostressObject(bandElement);
                            final Band bandObject = new EcostressBand(bandName, extractDataTypeOfProductData(bandElement), (int) bandElement.getWidth(), (int) bandElement.getHeight(), bandElement.getFullName());
                            final Attribute bandDescriptionAttribute = extractEcostressBandAttribute(bandElement, BAND_ATTRIBUTE_NAME_DESCRIPTION);
                            if (bandDescriptionAttribute != null) {
                                final String bandDescription = getEcostressAttributeValue(bandDescriptionAttribute).getElemString();
                                bandObject.setDescription(bandDescription);
                            }
                            final Attribute bandUnitAttribute = extractEcostressBandAttribute(bandElement, BAND_ATTRIBUTE_NAME_UNIT);
                            if (bandUnitAttribute != null) {
                                final String bandUnit = getEcostressAttributeValue(bandUnitAttribute).getElemString();
                                bandObject.setUnit(bandUnit);
                            }
                            final Attribute fillValueAttribute = extractEcostressBandAttribute(bandElement, BAND_ATTRIBUTE_NAME_FILL_VALUE);
                            if (fillValueAttribute != null) {
                                final double fillValue = getEcostressAttributeValue(fillValueAttribute).getElemDouble();
                                bandObject.setNoDataValue(fillValue);
                                bandObject.setNoDataValueUsed(true);
                            }
                            bandsList.add(bandObject);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Failed to extract the band object from ECOSTRESS product '" + ecostressFile.getName() + "'. Reason: " + e.getMessage());
            throw new IllegalStateException(e);
        }
        return bandsList;
    }

    /**
     * Reads the subset data from the ECOSTRESS product file band in the target buffer
     *
     * @param ecostressFile the ECOSTRESS product file
     * @param targetBand    the ECOSTRESS band
     * @param width         the width of the subset
     * @param height        the height of the subset
     * @param offsetX       the X offset of the subset
     * @param offsetY       the Y offset of the subset
     * @param destBuffer    the destination buffer where the subset data from the ECOSTRESS product file band is put
     */
    public static void readEcostressBandData(EcostressFile ecostressFile, Band targetBand, int width, int height, long offsetX, long offsetY, ProductData destBuffer, boolean isBandRasterReversed) {
        final EcostressBand ecostressBand = (EcostressBand) targetBand;
        final String bandElementPath = ecostressBand.getBandPathInEcostressProduct();
        final Object ecostressBandData;
        if (isBandRasterReversed) {
            offsetX = targetBand.getRasterWidth() - width - offsetX;
            offsetY = targetBand.getRasterHeight() - height - offsetY;
            ecostressBandData = readEcostressBandDataSubset(ecostressFile, bandElementPath, width, height, offsetX, offsetY);
            ArrayUtils.swapArray(ecostressBandData);
        } else {
            ecostressBandData = readEcostressBandDataSubset(ecostressFile, bandElementPath, width, height, offsetX, offsetY);
        }
        destBuffer.setElems(ecostressBandData);
    }

    /**
     * Reads the data from the ECOSTRESS product file band
     *
     * @param ecostressFile the ECOSTRESS product file
     * @param targetBand    the ECOSTRESS band
     * @return the data from the ECOSTRESS product file band
     */
    public static Object readAndGetEcostressBandData(EcostressFile ecostressFile, Band targetBand) {
        final EcostressBand ecostressBand = (EcostressBand) targetBand;
        final String bandElementPath = ecostressBand.getBandPathInEcostressProduct();
        return readAndGetEcostressBandData(ecostressFile, bandElementPath, targetBand.getRasterWidth(), targetBand.getRasterHeight());
    }

    /**
     * Extracts the date and time from an ECOSTRESS product file
     *
     * @param ecostressFile the ECOSTRESS product file
     * @param dateElementPath the metadata node path with date information in the ECOSTRESS product file
     * @param timeElementPath the metadata node path with time information in the ECOSTRESS product file
     * @return the date and time from an ECOSTRESS product file
     */
    private synchronized static ProductData.UTC extractDateTime(EcostressFile ecostressFile, String dateElementPath, String timeElementPath) {
        try (ecostressFile) {
            final H5ScalarDS date = getEcostressH5ScalarDS(ecostressFile, dateElementPath);
            final H5ScalarDS time = getEcostressH5ScalarDS(ecostressFile, timeElementPath);
            if (date != null && time != null) {
                final String dateValue = getEcostressH5ScalarDSValue(date).getElemString().trim();
                final String timeValue = getEcostressH5ScalarDSValue(time).getElemString().trim();
                return ProductData.UTC.parse(dateValue + " " + timeValue, EcostressConstants.ECOSTRESS_DATE_FORMAT_PATTERN);
            }
            return ProductData.UTC.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern(EcostressConstants.ECOSTRESS_DATE_FORMAT_PATTERN)), EcostressConstants.ECOSTRESS_DATE_FORMAT_PATTERN);
        } catch (Exception e) {
            logger.severe("Failed to extract product date-time from ECOSTRESS product '" + ecostressFile.getName() + "'. Reason: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Extracts the dimension from an ECOSTRESS product file
     * @param ecostressFile the ECOSTRESS product file
     * @param widthElementPath the metadata node path with width information in the ECOSTRESS product file
     * @param heightElementPath the metadata node path with height information in the ECOSTRESS product file
     * @return the dimension an ECOSTRESS product file
     */
    public synchronized static Dimension extractEcostressProductDimension(EcostressFile ecostressFile, String widthElementPath, String heightElementPath) {
        try (ecostressFile) {
            final H5ScalarDS width = getEcostressH5ScalarDS(ecostressFile, widthElementPath);
            final H5ScalarDS height = getEcostressH5ScalarDS(ecostressFile, heightElementPath);
            if (width != null && height != null) {
                final int widthValue = getEcostressH5ScalarDSValue(width).getElemInt();
                final int heightValue = getEcostressH5ScalarDSValue(height).getElemInt();
                return new Dimension(widthValue, heightValue);
            }
            return new Dimension(-1, -1);
        } catch (Exception e) {
            logger.severe("Failed to extract the dimension from ECOSTRESS product '" + ecostressFile.getName() + "' Reason: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Extracts the SNAP band object data type from an ECOSTRESS product file HDF5 scalar object
     * @param h5ScalarDS the ECOSTRESS product file HDF5 scalar object
     * @return the SNAP band object data type from an ECOSTRESS product file
     * @throws HDF5Exception if an error occurs
     */
    private static int extractDataTypeOfProductData(H5ScalarDS h5ScalarDS) throws Exception {
        final Object h5ScalarDSValueObject = h5ScalarDS.read();
        switch (h5ScalarDS.getDatatype().getDatatypeClass()) {
            case Datatype.CLASS_INTEGER:
                if (h5ScalarDSValueObject.getClass() == long[].class) {
                    return ProductData.TYPE_INT64;
                }
                if (h5ScalarDSValueObject.getClass() == int[].class) {
                    return ProductData.TYPE_INT32;
                }
                if (h5ScalarDSValueObject.getClass() == short[].class) {
                    return ProductData.TYPE_INT16;
                }
                if (h5ScalarDSValueObject.getClass() == byte[].class) {
                    return ProductData.TYPE_INT8;
                }
                break;
            case Datatype.CLASS_FLOAT:
                if (h5ScalarDSValueObject.getClass() == float[].class) {
                    return ProductData.TYPE_FLOAT32;
                }
                if (h5ScalarDSValueObject.getClass() == double[].class) {
                    return ProductData.TYPE_FLOAT64;
                }
                break;
            case Datatype.CLASS_CHAR:
                if (h5ScalarDSValueObject.getClass() == byte[].class) {
                    return ProductData.TYPE_INT8;
                }
                break;
            default:
                break;
        }
        return ProductData.TYPE_UNDEFINED;
    }

    /**
     * Extracts the HDF5 attribute from an ECOSTRESS product file HDF5 scalar object
     * @param h5ScalarDS the ECOSTRESS product file HDF5 scalar object
     * @param attributeName the attribute path in the ECOSTRESS product
     * @return the HDF5 attribute from an ECOSTRESS product file
     */
    private static Attribute extractEcostressBandAttribute(H5ScalarDS h5ScalarDS, String attributeName) {
        if (h5ScalarDS.hasAttribute()) {
            try {
                for (Object attributeObject : h5ScalarDS.getMetadata()) {
                    final Attribute attribute = (Attribute) attributeObject;
                    if (attribute.getAttributeName().equalsIgnoreCase(attributeName)) {
                        return attribute;
                    }
                }
            } catch (Exception e) {
                logger.warning("Failed to extract attribute '" + attributeName + "' from band '" + h5ScalarDS.getFullName() + "'");
            }
        }
        return null;
    }

    /**
     * Reads the subset data from the ECOSTRESS product file band
     * @param ecostressFile the ECOSTRESS product file
     * @param bandElementPath the ECOSTRESS band
     * @param width the width of the subset
     * @param height the height of the subset
     * @return the subset data from the ECOSTRESS product file band
     */
    private static Object readAndGetEcostressBandData(EcostressFile ecostressFile, String bandElementPath, int width, int height) {
        return readEcostressBandDataSubset(ecostressFile, bandElementPath, width, height, 0, 0);
    }

    /**
     * Reads the subset data from the ECOSTRESS product file band
     * @param ecostressFile the ECOSTRESS product file
     * @param bandElementPath the ECOSTRESS band
     * @param width the width of the subset
     * @param height the height of the subset
     * @param offsetX the X offset of the subset
     * @param offsetY the Y offset of the subset
     * @return the subset data from the ECOSTRESS product file band
     */
    private static synchronized Object readEcostressBandDataSubset(EcostressFile ecostressFile, String bandElementPath, int width, int height, long offsetX, long offsetY) {
        try (ecostressFile) {
            final H5ScalarDS bandElement = getEcostressH5ScalarDS(ecostressFile, bandElementPath);
            bandElement.init();
            final long[] offsets = bandElement.getStartDims();
            final long[] sizes = bandElement.getSelectedDims();
            offsets[0] = offsetY;
            if (offsets.length > 1) {
                offsets[1] = offsetX;
            }
            sizes[0] = height;
            if (sizes.length > 1) {
                sizes[1] = width;
            }
            return bandElement.read();
        } catch (Exception e) {
            logger.severe("Failed to read the band (" + bandElementPath + ") data subset from ECOSTRESS product '" + ecostressFile.getName() + "' with coordinates w=" + width + " h=" + height + " X=" + offsetX + " Y=" + offsetY + ". Reason: " + e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    /**
     * Gets the band name from the ECOSTRESS product file HDF5 scalar object
     * @param ecostressObject the ECOSTRESS product file HDF5 scalar object
     * @return the band name from the ECOSTRESS product file band
     */
    private static String getBandNameFromEcostressObject(H5ScalarDS ecostressObject) {
        String ecostressBandPathInProduct = Paths.get(ecostressObject.getPath()).getParent().toString().replaceAll("\\\\", "/");
        if (!ecostressBandPathInProduct.endsWith("/")) {
            ecostressBandPathInProduct += "/";
        }
        return ecostressObject.getFullName().replaceFirst(ecostressBandPathInProduct, "").replaceAll("/", "_").replaceAll(" ","_");
    }

    /**
     * Gets the HDF5 group object from the ECOSTRESS product file
     * @param ecostressFile the ECOSTRESS product file
     * @param nodePath the node path in the ECOSTRESS product file
     * @return the HDF5 group object from the ECOSTRESS product file
     */
    private static H5Group getEcostressH5Group(EcostressFile ecostressFile, String nodePath) {
        return (H5Group) getEcostressNode(ecostressFile, nodePath);
    }

    /**
     * Gets the HDF5 scalar object from the ECOSTRESS product file
     * @param ecostressFile the ECOSTRESS product file
     * @param nodePath the node path in the ECOSTRESS product file
     * @return the HDF5 scalar object from the ECOSTRESS product file
     */
    private static H5ScalarDS getEcostressH5ScalarDS(EcostressFile ecostressFile, String nodePath) {
        return (H5ScalarDS) getEcostressNode(ecostressFile, nodePath);
    }

    /**
     * Gets the HDF5 object from the ECOSTRESS product file
     * @param ecostressFile the ECOSTRESS product file
     * @param nodePath the node path in the ECOSTRESS product file
     * @return the HDF5 object from the ECOSTRESS product file
     */
    private static HObject getEcostressNode(EcostressFile ecostressFile, String nodePath) {
        try {
            return ecostressFile.getH5File().get(nodePath);
        } catch (Exception e) {
            logger.severe("Error reading ECOSTRESS node '" + nodePath + "' from product '" + ecostressFile.getName() + "': " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the HDF5 scalar object value from ECOSTRESS product file HDF5 scalar object
     *
     * @param ecostressScalar the ECOSTRESS product file HDF5 scalar object
     * @return the HDF5 scalar object value from ECOSTRESS product file HDF5 scalar object
     */
    private static ProductData getEcostressH5ScalarDSValue(H5ScalarDS ecostressScalar) throws Exception {
        final Object ecostressScalarValueObject = ecostressScalar.read();
        return extractEcostressObjectValue(ecostressScalar.getDatatype(), ecostressScalarValueObject.getClass(), ecostressScalarValueObject);
    }

    /**
     * Gets the HDF5 attribute object value from ECOSTRESS product file HDF5 attribute object
     * @param ecostressAttribute the ECOSTRESS product file HDF5 attribute object
     * @return the HDF5 attribute object value from ECOSTRESS product file HDF5 attribute object
     */
    private static ProductData getEcostressAttributeValue(Attribute ecostressAttribute) throws Exception {
        final Object ecostressAttributeValueObject = ecostressAttribute.getAttributeData();
        return extractEcostressObjectValue(ecostressAttribute.getAttributeDatatype(), ecostressAttributeValueObject.getClass(), ecostressAttributeValueObject);
    }

    /**
     * Extract the value from ECOSTRESS product file HDF5 scalar object
     * @param ecostressObjectDatatype the ECOSTRESS product file HDF5 object data type
     * @param ecostressObjectClass the ECOSTRESS product file HDF5 object class
     * @param ecostressObject the ECOSTRESS product file HDF5 object
     * @return the value from ECOSTRESS product file HDF5 scalar object
     */
    private static ProductData extractEcostressObjectValue(Datatype ecostressObjectDatatype, Class<?> ecostressObjectClass, Object ecostressObject) {
        switch (ecostressObjectDatatype.getDatatypeClass()) {
            case Datatype.CLASS_INTEGER:
                if (ecostressObjectClass == long[].class) {
                    final long[] ecostressObjectValues = (long[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                if (ecostressObjectClass == int[].class) {
                    final int[] ecostressObjectValues = (int[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                if (ecostressObjectClass == short[].class) {
                    final short[] ecostressObjectValues = (short[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                if (ecostressObjectClass == byte[].class) {
                    final byte[] ecostressObjectValues = (byte[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                break;
            case Datatype.CLASS_FLOAT:
                if (ecostressObjectClass == float[].class) {
                    final float[] ecostressObjectValues = (float[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                if (ecostressObjectClass == double[].class) {
                    final double[] ecostressObjectValues = (double[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                break;
            case Datatype.CLASS_CHAR:
                if (ecostressObjectClass == byte[].class) {
                    final byte[] ecostressObjectValues = (byte[]) ecostressObject;
                    return ProductData.createInstance(ecostressObjectValues);
                }
                break;
            case Datatype.CLASS_STRING:
                if (ecostressObjectClass == String[].class) {
                    final String[] ecostressObjectValues = (String[]) ecostressObject;
                    final String ecostressObjectResultValues = aggregateStringArray(ecostressObjectValues);
                    return ProductData.createInstance(ecostressObjectResultValues);
                }
                break;
            default:
                break;
        }
        return ProductData.createInstance("");
    }

    /**
     * Aggregates the String array in single String object
     * @param ecostressObjectValues the String array
     * @return the aggregated String value
     */
    private static String aggregateStringArray(String[] ecostressObjectValues) {
        final StringBuilder ecostressObjectResultValues = new StringBuilder();
        for (String ecostressObjectValue : ecostressObjectValues) {
            if (ecostressObjectResultValues.length() > 0) {
                ecostressObjectResultValues.append(" ");
            }
            ecostressObjectResultValues.append(ecostressObjectValue);
        }
        return ecostressObjectResultValues.toString();
    }
}
