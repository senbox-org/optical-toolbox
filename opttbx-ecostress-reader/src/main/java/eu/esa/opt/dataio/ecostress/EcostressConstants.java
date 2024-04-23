package eu.esa.opt.dataio.ecostress;

/**
 * Constants for reading ECOSTRESS products
 *
 * @author adraghici
 */
public class EcostressConstants {

    public static final String ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA = "/StandardMetadata";
    public static final String ECOSTRESS_STANDARD_METADATA_RANGE_BEGINNING_DATE = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeBeginningDate";
    public static final String ECOSTRESS_STANDARD_METADATA_RANGE_BEGINNING_TIME = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeBeginningTime";
    public static final String ECOSTRESS_STANDARD_METADATA_RANGE_ENDING_DATE = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeEndingDate";
    public static final String ECOSTRESS_STANDARD_METADATA_RANGE_ENDING_TIME = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/RangeEndingTime";
    public static final String ECOSTRESS_STANDARD_METADATA_IMAGE_LINES = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/ImageLines";
    public static final String ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS = ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA + "/ImagePixels";
    public static final String ECOSTRESS_STANDARD_METADATA_EAST_BOUNDING_COORDINATE = "EastBoundingCoordinate";
    public static final String ECOSTRESS_STANDARD_METADATA_WEST_BOUNDING_COORDINATE = "WestBoundingCoordinate";
    public static final String ECOSTRESS_STANDARD_METADATA_NORTH_BOUNDING_COORDINATE = "NorthBoundingCoordinate";
    public static final String ECOSTRESS_STANDARD_METADATA_SOUTH_BOUNDING_COORDINATE = "SouthBoundingCoordinate";

    public static final String ECOSTRESS_LATITUDE_BAND_NAME = "latitude";
    public static final String ECOSTRESS_LONGITUDE_BAND_NAME = "longitude";

    public static final String ECOSTRESS_DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String REMOTE_REPOSITORY_NAME = "USGS";
    public static final String REMOTE_MISSION_NAME = "ECOSTRESS";
}
