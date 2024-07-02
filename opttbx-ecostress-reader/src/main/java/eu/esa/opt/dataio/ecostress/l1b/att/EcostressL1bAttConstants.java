package eu.esa.opt.dataio.ecostress.l1b.att;

/**
 * Constants for ECOSTRESS L1B ATT products
 *
 * @author adraghici
 */
class EcostressL1bAttConstants {

    static final String ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS = "/Ephemeris";
    static final String ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE = "/Attitude";
    static final String ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS = "/Uncorrected Ephemeris";
    static final String ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE = "/Uncorrected Attitude";
    static final String ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA = "/L1GEOMetadata";

    static final String ECOSTRESS_L1B_ATT_REMOTE_PLATFORM_NAME = "ecostress_eco1batt";
    static final String ECOSTRESS_L1B_ATT_PRODUCT_FILE_NAME_PATTERN = "ECOSTRESS_L1B_ATT_\\d{5}_\\d{8}T\\d{6}_\\d{4}_\\d{2}.h5";
}
