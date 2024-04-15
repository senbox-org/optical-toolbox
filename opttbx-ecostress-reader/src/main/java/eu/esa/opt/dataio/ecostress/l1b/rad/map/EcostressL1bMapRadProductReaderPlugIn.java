package eu.esa.opt.dataio.ecostress.l1b.rad.map;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L1B MAP RAD products
 *
 * @author adraghici
 */
public class EcostressL1bMapRadProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_MAP_RAD = "ECOSTRESS-L1B-MAP-RAD";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL1bMapRadProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_MAP_RAD;
    }


}
