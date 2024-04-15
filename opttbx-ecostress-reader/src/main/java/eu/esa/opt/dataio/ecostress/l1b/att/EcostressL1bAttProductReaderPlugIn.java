package eu.esa.opt.dataio.ecostress.l1b.att;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L1B ATT products
 *
 * @author adraghici
 */
public class EcostressL1bAttProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_ATT = "ECOSTRESS-L1B-ATT";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL1bAttProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_ATT;
    }


}
