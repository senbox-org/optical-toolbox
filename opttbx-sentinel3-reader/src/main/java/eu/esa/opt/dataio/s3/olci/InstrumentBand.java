package eu.esa.opt.dataio.s3.olci;

import eu.esa.opt.dataio.s3.BandUsingReaderDirectly;
import eu.esa.opt.dataio.s3.util.SensorContext;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;

import java.io.IOException;

public class InstrumentBand extends BandUsingReaderDirectly {

    private ProductData productData;
    private SensorContext sensorContext;
    private ReaderContext readerContext;

    public InstrumentBand(String name, int dataType, int width, int height) {
        super(name, dataType, width, height);
        sensorContext = null;
    }

    public void setSensorContext(SensorContext sensorContext) {
        this.sensorContext = sensorContext;
    }

    public void setReaderContext(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void readRasterData(int offsetX, int offsetY, int width, int height, ProductData rasterData) throws IOException {
        if (readerContext.hasData(getName())) {
            // create subset from cached data
            // ensure target buffer size (is this done elsewhere?)
            // copy to raster data
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void readRasterDataFully() throws IOException {
        final String bandName = getName();
        if (!readerContext.hasData(bandName)) {
            final Array instrumentDataArray = readerContext.readData(bandName);
            final Array detectorIndex = readerContext.readData("detector_index");
            // create productData
            productData = ProductData.createInstance(ProductData.TYPE_FLOAT32, getRasterWidth() * getRasterHeight());
        }


    }
}
