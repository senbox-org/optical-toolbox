package eu.esa.opt.dataio.ecostress.l4.wue;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L4 WUE products
 *
 * @author adraghici
 */
public class EcostressL4WueProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L4_WUE = "ECOSTRESS-L4-WUE";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_L4_WUE_METADATA, EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_WATER_USE_EFFICIENCY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL4WueProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_WUE;
    }


}
