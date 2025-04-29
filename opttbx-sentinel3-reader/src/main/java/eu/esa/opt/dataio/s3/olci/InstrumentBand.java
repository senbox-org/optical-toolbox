package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.BandUsingReaderDirectly;
import eu.esa.opt.dataio.s3.util.SensorContext;
import org.esa.snap.core.datamodel.ProductData;

public class InstrumentBand extends BandUsingReaderDirectly {

    private ProductData productData;
    private SensorContext sensorContext;

    public InstrumentBand(String name, int dataType, int width, int height) {
        super(name, dataType, width, height);
        sensorContext = null;
    }

    void setSensorContext(SensorContext sensorContext) {
        this.sensorContext = sensorContext;
    }



}
