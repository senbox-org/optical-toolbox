package eu.esa.opt.dataio.ecostress.l1b.geo;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L1B GEO products
 *
 * @author adraghici
 */
public class EcostressL1bGeoProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_GEO = "ECOSTRESS-L1B-GEO";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_GEOLOCATION, EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL1bGeoProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_GEO;
    }


}
