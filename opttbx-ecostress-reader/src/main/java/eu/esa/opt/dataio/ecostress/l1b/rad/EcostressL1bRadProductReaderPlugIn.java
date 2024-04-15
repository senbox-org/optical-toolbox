package eu.esa.opt.dataio.ecostress.l1b.rad;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L1B RAD products
 *
 * @author adraghici
 */
public class EcostressL1bRadProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_LIB_RAD = "ECOSTRESS-L1B-RAD";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1B_RAD_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL1bRadProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_LIB_RAD;
    }


}
