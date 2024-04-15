package eu.esa.opt.dataio.ecostress.l3.et.alexi;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L3 ET ALEXI products
 *
 * @author adraghici
 */
public class EcostressL3AlexiProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI = "ECOSTRESS-L3-ET-ALEXI";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L3_ET_ALEXI_METADATA, EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_ALEXI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL3AlexiProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI;
    }


}
