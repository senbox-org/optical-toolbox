package eu.esa.opt.dataio.ecostress.l4.esi.alexi;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L4 ESI ALEXI products
 *
 * @author adraghici
 */
public class EcostressL4EsiAlexiProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL4EsiAlexiProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_ALEXI_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_X.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_X);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4EsiAlexiConstants.ECOSTRESS_L4_ESI_ALEXI_REMOTE_PLATFORM_NAME;
    }
}
