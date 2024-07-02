package eu.esa.opt.dataio.ecostress.l3.l4qa;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L3 L4 QA products
 *
 * @author adraghici
 */
public class EcostressL3L4QaMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L3_L4_QA = "ECOSTRESS-L3-L4-QA";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_DATA_DEFINITIONS_GROUP_L3_L4_QA_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_DATA_DEFINITIONS_GROUP_X.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_DATA_DEFINITIONS_GROUP_X};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_L4_QA;
    }
}
