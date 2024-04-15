package eu.esa.opt.dataio.ecostress.l3.l4qa;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L3 L4 QA products
 *
 * @author adraghici
 */
public class EcostressL3L4QaProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L3_L4_QA = "ECOSTRESS-L3-L4-QA";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_DATA_DEFINITIONS_GROUP_L3_L4_QA_METADATA, EcostressL3L4QaConstants.ECOSTRESS_L3_L4_QA_PRODUCT_DATA_DEFINITIONS_GROUP_X);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL3L4QaProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_L4_QA;
    }


}
