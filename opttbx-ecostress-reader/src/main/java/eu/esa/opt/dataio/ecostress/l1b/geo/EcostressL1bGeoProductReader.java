package eu.esa.opt.dataio.ecostress.l1b.geo;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L1B GEO products
 *
 * @author adraghici
 */
public class EcostressL1bGeoProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected EcostressL1bGeoProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_GEOLOCATION.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_PRODUCT_DATA_DEFINITIONS_GROUP_GEOLOCATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL1bGeoConstants.ECOSTRESS_L1B_GEO_REMOTE_PLATFORM_NAME;
    }

}
