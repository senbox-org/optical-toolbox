package eu.esa.opt.dataio.ecostress.l1b.att;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L1B ATT products
 *
 * @author adraghici
 */
class EcostressL1bAttProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected EcostressL1bAttProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        final String groupingPattern = EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS + ":" + EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE.replaceAll("/", "");
        return groupingPattern.replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_ATTITUDE, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_EPHEMERIS, EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_PRODUCT_DATA_DEFINITIONS_GROUP_UNCORRECTED_ATTITUDE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bAttConstants.ECOSTRESS_L1B_ATT_REMOTE_PLATFORM_NAME;
    }

}
