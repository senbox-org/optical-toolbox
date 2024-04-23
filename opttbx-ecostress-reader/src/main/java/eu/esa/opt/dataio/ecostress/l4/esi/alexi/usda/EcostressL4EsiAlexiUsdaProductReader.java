package eu.esa.opt.dataio.ecostress.l4.esi.alexi.usda;

import eu.esa.opt.dataio.ecostress.l4.esi.alexi.EcostressL4EsiAlexiProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;

/**
 * Reader for ECOSTRESS L4 ESI ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiUsdaProductReader extends EcostressL4EsiAlexiProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL4EsiAlexiUsdaProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    protected String getRemotePlatformName() {
        return EcostressL4EsiAlexiUsdaConstants.ECOSTRESS_L4_ESI_ALEXI_USDA_REMOTE_PLATFORM_NAME;
    }

}
