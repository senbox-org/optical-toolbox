package eu.esa.opt.dataio.ecostress.l4.wue;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L4 WUE products
 *
 * @author adraghici
 */
public class EcostressL4WueProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL4WueProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_L4_WUE_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_WATER_USE_EFFICIENCY.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL4WueConstants.ECOSTRESS_L4_WUE_PRODUCT_DATA_DEFINITIONS_GROUP_WATER_USE_EFFICIENCY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL4WueConstants.ECOSTRESS_L4_WUE_REMOTE_PLATFORM_NAME;
    }
}
