package eu.esa.opt.dataio.ecostress.l1b.rad.map;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L1B MAP RAD products
 *
 * @author adraghici
 */
public class EcostressL1bMapRadProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL1bMapRadProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED + ":" + EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAP_INFORMATION + ":" + EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_L1_GEO_METADATA.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL1bMapRadConstants.ECOSTRESS_L1B_MAP_RAD_PRODUCT_DATA_DEFINITIONS_GROUP_MAPPED);
    }


}
