package eu.esa.opt.dataio.ecostress.l1b.rad;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L1B RAD products
 *
 * @author adraghici
 */
public class EcostressL1bRadMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_RAD = "ECOSTRESS-L1B-RAD";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1B_RAD_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_RAD;
    }
}