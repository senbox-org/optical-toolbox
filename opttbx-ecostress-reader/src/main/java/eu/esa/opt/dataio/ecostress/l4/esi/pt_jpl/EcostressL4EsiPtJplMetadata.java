package eu.esa.opt.dataio.ecostress.l4.esi.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L4 ESI PT JPL products
 *
 * @author adraghici
 */
public class EcostressL4EsiPtJplMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_PT_JPL = "ECOSTRESS-L4-ESI-PT-JPL";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_PT_JPL_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPORATIVE_STRESS_INDEX_PT_JPL.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPORATIVE_STRESS_INDEX_PT_JPL};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_PT_JPL;
    }
}
