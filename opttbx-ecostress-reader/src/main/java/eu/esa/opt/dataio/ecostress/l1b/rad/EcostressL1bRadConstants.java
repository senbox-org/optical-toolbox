package eu.esa.opt.dataio.ecostress.l1b.rad;

/**
 * Constants for ECOSTRESS L1B RAD products
 *
 * @author adraghici
 */
class EcostressL1bRadConstants {

    static final String ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE = "/Radiance";
    static final String ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR = "/SWIR";
    static final String ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER = "/FPIEencoder";
    static final String ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME = "/Time";
    static final String ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1B_RAD_METADATA = "/L1B_RADMetadata";
    static final String ECOSTRESS_L1B_RAD_REMOTE_PLATFORM_NAME = "ecostress_eco1brad";
    static final String ECOSTRESS_L1B_RAD_PRODUCT_FILE_NAME_PATTERN = "ECOSTRESS_L1B_RAD_\\d{5}_\\d{3}_\\d{8}T\\d{6}_\\d{4}_\\d{2}.h5";
}
