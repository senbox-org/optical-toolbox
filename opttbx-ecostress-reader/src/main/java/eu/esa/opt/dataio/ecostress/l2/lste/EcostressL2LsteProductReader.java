package eu.esa.opt.dataio.ecostress.l2.lste;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L2 LSTE products
 *
 * @author adraghici
 */
public class EcostressL2LsteProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL2LsteProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_L2_LSTE_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_SDS.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_PRODUCT_DATA_DEFINITIONS_GROUP_SDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRemotePlatformName() {
        return EcostressL2LsteConstants.ECOSTRESS_L2_LSTE_REMOTE_PLATFORM_NAME;
    }
}
