package eu.esa.opt.dataio.ecostress.l4.esi.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L4 ESI PT JPL products
 *
 * @author adraghici
 */
public class EcostressL4EsiPtJplProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_PT_JPL = "ECOSTRESS-L4-ESI-PT-JPL";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_PT_JPL_METADATA, EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPORATIVE_STRESS_INDEX_PT_JPL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL4EsiPtJplProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_PT_JPL;
    }


}
