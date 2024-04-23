package eu.esa.opt.dataio.ecostress.l1b.rad;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L1B RAD products
 *
 * @author adraghici
 */
public class EcostressL1bRadProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL1bRadProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1B_RAD_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER + ":" + EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_RADIANCE, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_SWIR, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_FPIE_ENCODER, EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bRadConstants.ECOSTRESS_L1B_RAD_REMOTE_PLATFORM_NAME;
    }
}
