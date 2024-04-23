package eu.esa.opt.dataio.ecostress.l3.et.alexi;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L3 ET ALEXI products
 *
 * @author adraghici
 */
public class EcostressL3AlexiProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL3AlexiProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_L3_ET_ALEXI_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_ALEXI.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_ALEXI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL3AlexiConstants.ECOSTRESS_L3_ET_ALEXI_REMOTE_PLATFORM_NAME;
    }
}
