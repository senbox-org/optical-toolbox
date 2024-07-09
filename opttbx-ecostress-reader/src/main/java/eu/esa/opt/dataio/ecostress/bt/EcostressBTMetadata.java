package eu.esa.opt.dataio.ecostress.bt;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS BrightnessTemperature products
 *
 * @author adraghici
 */
public class EcostressBTMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESSBT = "EcostressBrightnessTemperature";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        final String groupingPattern = EcostressBTConstants.ECOSTRESS_BT_PRODUCT_DATA_DEFINITIONS_GROUP_LUT.replaceAll("/", "");
        return groupingPattern.replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressBTConstants.ECOSTRESS_BT_PRODUCT_DATA_DEFINITIONS_GROUP_LUT};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESSBT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressBTConstants.ECOSTRESS_BT_PRODUCT_FILE_NAME_PATTERN;
    }
}
