package eu.esa.opt.dataio.ecostress.l3.et.alexi.usda;

import eu.esa.opt.dataio.ecostress.l3.et.alexi.EcostressL3AlexiProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;

/**
 * Reader for ECOSTRESS L3 ET ALEXI USDA products
 *
 * @author adraghici
 */
public class EcostressL3AlexiUsdaProductReader extends EcostressL3AlexiProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL3AlexiUsdaProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3AlexiUsdaConstants.ECOSTRESS_L3_ET_ALEXI_USDA_REMOTE_PLATFORM_NAME;
    }

}
