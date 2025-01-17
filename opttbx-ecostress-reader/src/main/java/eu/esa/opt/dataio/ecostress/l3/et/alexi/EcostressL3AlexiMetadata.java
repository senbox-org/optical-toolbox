package eu.esa.opt.dataio.ecostress.l3.et.alexi;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L3 ET ALEXI products
 *
 * @author adraghici
 */
public class EcostressL3AlexiMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI = "ECOSTRESS-L3-ET-ALEXI";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L3_ET_ALEXI_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_ALEXI.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_ALEXI};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI;
    }
}
