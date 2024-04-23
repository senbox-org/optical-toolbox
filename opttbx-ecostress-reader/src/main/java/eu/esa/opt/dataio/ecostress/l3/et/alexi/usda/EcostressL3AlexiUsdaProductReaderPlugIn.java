package eu.esa.opt.dataio.ecostress.l3.et.alexi.usda;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.l3.et.alexi.EcostressL3AlexiProductReaderPlugIn;

/**
 * Reader plug-in for ECOSTRESS L3 ET ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL3AlexiUsdaProductReaderPlugIn extends EcostressL3AlexiProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI_USDA = "ECOSTRESS-L3-ET-ALEXI-USDA";

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL3AlexiUsdaProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L3_ET_ALEXI_USDA;
    }
}
