package eu.esa.opt.dataio.ecostress.l4.esi.alexi.usda;

import eu.esa.opt.dataio.ecostress.l4.esi.alexi.EcostressL4EsiAlexiMetadata;

/**
 * Metadata for ECOSTRESS L4 ESI ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiUsdaMetadata extends EcostressL4EsiAlexiMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI_USDA = "ECOSTRESS-L4_ESI_ALEXI-USDA";

    /**
     * {@inheritDoc}
     */
    protected String getRemotePlatformName() {
        return EcostressL4EsiAlexiUsdaConstants.ECOSTRESS_L4_ESI_ALEXI_USDA_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL4EsiAlexiUsdaConstants.ECOSTRESS_L4_ESI_ALEXI_USDA_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI_USDA;
    }
}