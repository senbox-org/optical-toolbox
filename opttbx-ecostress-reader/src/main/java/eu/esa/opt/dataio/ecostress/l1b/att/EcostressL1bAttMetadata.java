package eu.esa.opt.dataio.ecostress.l1b.att;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L1B ATT products
 *
 * @author adraghici
 */
public class EcostressL1bAttMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_ATT = "ECOSTRESS-L1B-ATT";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        final String groupingPattern = EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE.replaceAll("/", "");
        return groupingPattern.replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_ATT;
    }
}