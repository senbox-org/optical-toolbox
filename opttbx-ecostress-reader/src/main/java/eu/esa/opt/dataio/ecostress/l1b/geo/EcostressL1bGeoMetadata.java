package eu.esa.opt.dataio.ecostress.l1b.geo;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L1B GEO products
 *
 * @author adraghici
 */
public class EcostressL1bGeoMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_GEO = "ECOSTRESS-L1B-GEO";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_GEOLOCATION.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_GEOLOCATION};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_FILE_NAME_PATTERN;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_GEO;
    }
}