package eu.esa.opt.dataio.ecostress.l3.et.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressMetadata;

/**
 * Metadata for ECOSTRESS L3 ET PT JPL products
 *
 * @author adraghici
 */
public class EcostressL3EtPtJplMetadata extends EcostressMetadata {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_PT_JPL = "ECOSTRESS-L3_ET_PT_JPL";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getMetadataElementsPaths() {
        return new String[]{EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L3_PT_JPL_METADATA};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_PT_JPL.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getBandsElementsPaths() {
        return new String[]{EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_PT_JPL};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_REMOTE_PLATFORM_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getProductFileNameRegex() {
        return EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_FILE_NAME_PATTERN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_PT_JPL;
    }
}
