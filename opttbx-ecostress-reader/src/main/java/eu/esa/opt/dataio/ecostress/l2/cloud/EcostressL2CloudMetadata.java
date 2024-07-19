package eu.esa.opt.dataio.ecostress.l2.cloud;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L2 Cloud products
 *
 * @author adraghici
 */
public class EcostressL2CloudMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_CLOUD = "ECOSTRESS-L2-CLOUD";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL2CloudConstants.ECOSTRESS_L2_CLOUD_PRODUCT_DATA_DEFINITIONS_GROUP_L2_CLOUD_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL2CloudConstants.ECOSTRESS_L2_CLOUD_PRODUCT_DATA_DEFINITIONS_GROUP_SDS.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL2CloudConstants.ECOSTRESS_L2_CLOUD_PRODUCT_DATA_DEFINITIONS_GROUP_SDS};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL2CloudConstants.ECOSTRESS_L2_CLOUD_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL2CloudConstants.ECOSTRESS_L2_CLOUD_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_CLOUD;
    }
}
