package eu.esa.opt.dataio.ecostress;

/**
 * Constants for reading ECOSTRESS products
 *
 * @author adraghici
 */
public class EcostressConstants {

    public static final String ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA = "/StandardMetadata";
    public static final String ECOSTRESS_STANDARD_METADATA_Range_Beginning_Date = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeBeginningDate";
    public static final String ECOSTRESS_STANDARD_METADATA_Range_Beginning_Time = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeBeginningTime";
    public static final String ECOSTRESS_STANDARD_METADATA_Range_Ending_Date = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeEndingDate";
    public static final String ECOSTRESS_STANDARD_METADATA_Range_Ending_Time = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeEndingTime";
    public static final String ECOSTRESS_STANDARD_METADATA_IMAGE_LINES = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/ImageLines";
    public static final String ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/ImagePixels";

    public static final String ECOSTRESS_LATITUDE_BAND_NAME="latitude";
    public static final String ECOSTRESS_LONGITUDE_BAND_NAME="longitude";

    public static final String ECOSTRESS_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
}
