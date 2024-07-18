package eu.esa.opt.dataio.ecostress.l4.wue;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L4 WUE products
 *
 * @author adraghici
 */
public class EcostressL4WueMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L4_WUE = "ECOSTRESS-L4-WUE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_L4_WUE_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_WATER_USE_EFFICIENCY.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_WATER_USE_EFFICIENCY};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4WueConstants.ECOSTRESS_L4_WUE_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_WUE;
    }
}
