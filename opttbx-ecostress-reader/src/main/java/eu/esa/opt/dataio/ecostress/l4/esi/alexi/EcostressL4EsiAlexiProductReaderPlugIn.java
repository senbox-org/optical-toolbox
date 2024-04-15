package eu.esa.opt.dataio.ecostress.l4.esi.alexi;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReaderPlugIn;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;

/**
 * Reader plug-in for ECOSTRESS L4 ESI ALEXI products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiProductReaderPlugIn extends EcostressAbstractProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI = "ECOSTRESS-L4_ESI_ALEXI";

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isInputContentValid(EcostressFile ecostressFile) {
        return EcostressUtils.ecostressNodesExists(ecostressFile, EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_ALEXI_METADATA, EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_X);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL4EsiAlexiProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI;
    }


}
