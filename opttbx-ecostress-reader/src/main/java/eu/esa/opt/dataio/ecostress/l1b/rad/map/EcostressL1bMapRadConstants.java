package eu.esa.opt.dataio.ecostress.l1b.rad.map;

/**
 * Constants for ECOSTRESS L1B MAP RAD products
 *
 * @author adraghici
 */
class EcostressL1bMapRadConstants {

    static final String ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED = "/Mapped";
    static final String ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION = ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED + "/MapInformation";
    static final String ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA = "/L1GEOMetadata";
    static final String ECOSTRESS_L1B_MAP_RAD_REMOTE_PLATFORM_NAME = "ecostress_eco1bmaprad";
    static final String ECOSTRESS_L1B_MAP_RAD_PRODUCT_FILE_NAME_PATTERN = "ECOSTRESS_L1B_MAP_RAD_\\d{5}_\\d{3}_\\d{8}T\\d{6}_\\d{4}_\\d{2}.h5";
}
