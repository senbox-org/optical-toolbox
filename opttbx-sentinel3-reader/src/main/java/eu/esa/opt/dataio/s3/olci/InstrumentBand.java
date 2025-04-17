package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.BandUsingReaderDirectly;
import org.esa.snap.core.datamodel.ProductData;

public class InstrumentBand extends BandUsingReaderDirectly {

    private ProductData productData;

    public InstrumentBand(String name, int dataType, int width, int height) {
        super(name, dataType, width, height);
    }


}
