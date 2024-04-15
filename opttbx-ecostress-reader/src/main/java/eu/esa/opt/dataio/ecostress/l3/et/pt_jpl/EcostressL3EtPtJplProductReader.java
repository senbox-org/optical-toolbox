package eu.esa.opt.dataio.ecostress.l3.et.pt_jpl;

import eu.esa.opt.dataio.ecostress.EcostressAbstractProductReader;
import eu.esa.opt.dataio.ecostress.EcostressFile;
import eu.esa.opt.dataio.ecostress.EcostressUtils;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.MetadataElement;

import java.util.List;

/**
 * Reader for ECOSTRESS L3 ET PT JPL products
 *
 * @author adraghici
 */
public class EcostressL3EtPtJplProductReader extends EcostressAbstractProductReader {

    /**
     * Constructs a new product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader implementations
     */
    protected EcostressL3EtPtJplProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_L3_PT_JPL_METADATA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getGroupingPattern() {
        return EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_PT_JPL.replaceAll("/", "").replaceAll(" ", "__");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Band> getBandsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractBandsObjects(ecostressFile, EcostressL3EtPtJplConstants.ECOSTRESS_L3_ET_PT_JPL_PRODUCT_DATA_DEFINITIONS_GROUP_EVAPOTRANSPIRATION_PT_JPL);
    }


}
