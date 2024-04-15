package eu.esa.opt.dataio.ecostress.l3.et.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L3 ET PT JPL products
 *
 * @author adraghici
 */
public class EcostressL3EtPtJplProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_PT_JPL = "ECOSTRESS-L3_ET_PT_JPL";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L3_PT_JPL_METADATA, EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_PT_JPL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL3EtPtJplProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_PT_JPL;
    }


}
