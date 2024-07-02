package eu.esa.opt.dataio.ecostress.l4.esi.alexi;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L4 ESI ALEXI products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI = "ECOSTRESS-L4_ESI_ALEXI";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_ALEXI_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_X.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_X};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI;
    }
}
