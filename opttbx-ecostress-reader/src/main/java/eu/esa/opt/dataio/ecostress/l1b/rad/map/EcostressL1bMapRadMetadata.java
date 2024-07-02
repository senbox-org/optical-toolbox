package eu.esa.opt.dataio.ecostress.l1b.rad.map;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L1B MAP RAD products
 *
 * @author adraghici
 */
public class EcostressL1bMapRadMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_MAP_RAD = "ECOSTRESS-L1B-MAP-RAD";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED + ":" + EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION + ":" + EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_MAP_RAD;
    }
}