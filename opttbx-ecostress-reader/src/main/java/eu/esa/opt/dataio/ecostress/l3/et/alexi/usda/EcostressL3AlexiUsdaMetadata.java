package eu.esa.opt.dataio.ecostress.l3.et.alexi.usda;

import eu.esa.opt.dataio.ecostress.l3.et.alexi.EcostressL3AlexiMetadata;

/**
 * Metadata for ECOSTRESS L3 ET ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL3AlexiUsdaMetadata extends EcostressL3AlexiMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI_USDA = "ECOSTRESS-L3-ET-ALEXI-USDA";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3AlexiUsdaConstants.ECOSTRESS_L3_ET_ALEXI_USDA_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL3AlexiUsdaConstants.ECOSTRESS_L3_ET_ALEXI_USDA_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI_USDA;
    }
}
