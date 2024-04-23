package eu.esa.opt.dataio.ecostress.l4.esi.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L4 ESI PT JPL products
 *
 * @author adraghici
 */
public class EcostressL4EsiPtJplProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL4EsiPtJplProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L4_ESI_PT_JPL_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPORATIVE_STRESS_INDEX_PT_JPL.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPORATIVE_STRESS_INDEX_PT_JPL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4EsiPtJplConstants.ECOSTRESS_L4_ESI_PT_JPL_REMOTE_PLATFORM_NAME;
    }
}
