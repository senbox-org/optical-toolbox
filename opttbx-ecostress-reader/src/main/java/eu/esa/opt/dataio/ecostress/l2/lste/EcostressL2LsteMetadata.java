package eu.esa.opt.dataio.ecostress.l2.lste;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L2 LSTE products
 *
 * @author adraghici
 */
public class EcostressL2LsteMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L2_LSTE = "ECOSTRESS-L2-LSTE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_L2_LSTE_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_SDS.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_SDS};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L2_LSTE;
    }
}
