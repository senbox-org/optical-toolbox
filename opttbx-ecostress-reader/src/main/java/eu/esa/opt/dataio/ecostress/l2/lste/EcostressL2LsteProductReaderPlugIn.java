package eu.esa.opt.dataio.ecostress.l2.lste;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L2 LSTE products
 *
 * @author adraghici
 */
public class EcostressL2LsteProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L2_LSTE = "ECOSTRESS-L2-LSTE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_L2_LSTE_METADATA, EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_SDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL2LsteProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L2_LSTE;
    }


}
