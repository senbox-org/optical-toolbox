package eu.esa.opt.dataio.ecostress.l4.esi.alexi.usda;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.l4.esi.alexi.EcostressL4EsiAlexiProductReaderPlugIn;

/**
 * Reader plug-in for ECOSTRESS L4 ESI ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiUsdaProductReaderPlugIn extends EcostressL4EsiAlexiProductReaderPlugIn {

    public static final String FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI_USDA = "ECOSTRESS-L4_ESI_ALEXI-USDA";

    /**
     * {@inheritDoc}
     */
    @Override
    public EcostressAbstractProductReader createReaderInstance() {
        return new EcostressL4EsiAlexiUsdaProductReader(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormatName() {
        return FORMAT_NAME_ECOSTRESS_L4_ESI_ALEXI_USDA;
    }
}
