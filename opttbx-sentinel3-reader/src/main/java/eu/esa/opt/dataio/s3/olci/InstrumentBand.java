package eu.esa.opt.dataio.s3.olci;

import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.dataio.s3.BandUsingReaderDirectly;
import eu.esa.opt.dataio.s3.Sentinel3Level1Reader;
import eu.esa.snap.core.dataio.RasterExtract;
import org.esa.snap.core.datamodel.ProductData;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;

public class InstrumentBand extends BandUsingReaderDirectly {

    private static final int NUM_DETECTORS = 3700;

    private Array productData;
    private ReaderContext readerContext;

    public InstrumentBand(String name, int dataType, int width, int height) {
        super(name, dataType, width, height);
        productData = null;
    }

    public void setReaderContext(ReaderContext readerContext) {
        this.readerContext = readerContext;
    }

    @Override
    public void readRasterData(final int offsetX, final int offsetY,
                               final int width, final int height,
                               final ProductData rasterData,
                               ProgressMonitor pm) throws IOException {

        readRasterData(offsetX, offsetY, width, height, rasterData);
    }


    @Override
    public void readRasterData(int offsetX, int offsetY, int width, int height, ProductData rasterData) throws IOException {
        readRasterDataFully();

        // Define subset to requested region
        final RasterExtract rasterExtract = new RasterExtract(offsetX, offsetY, width, height, 1, 1);
        Sentinel3Level1Reader.extractSubset(rasterExtract, rasterData, productData, getScalingFactor(), getScalingOffset(), true);
    }

    @Override
    public void readRasterDataFully() throws IOException {
        final String bandName = getName();

        synchronized (readerContext.getBandLock(bandName)) {
            if (!readerContext.hasData(bandName)) {
                // extract variable name from band name and layer index
                final String instrumentVariableName = Sentinel3Level1Reader.getVariableNameFromLayerName(bandName);
                final int layerIndex = Sentinel3Level1Reader.getLayerIndexFromLayerName(bandName) - 1;

                final Array instrumentDataArray = readerContext.readData(instrumentVariableName);

                final Array layerVector;
                if (layerIndex >= 0) {

                    try {
                        // subset to layer
                        final int[] origin = {layerIndex, 0};
                        final int[] shape = {1, NUM_DETECTORS};
                        layerVector = instrumentDataArray.section(origin, shape);
                    } catch (InvalidRangeException e) {
                        throw new IOException(e);
                    }
                } else {
                    layerVector = instrumentDataArray;
                }

                final Array detectorIndex = readerContext.readData("detector_index");

                // create productData
                productData = Array.factory(DataType.FLOAT, new int[]{getRasterHeight(), getRasterWidth()});

                final DataMapper dataMapper = new DataMapper();
                dataMapper.mapData((float[]) layerVector.get1DJavaArray(DataType.FLOAT),
                        (float[]) productData.get1DJavaArray(DataType.FLOAT),
                        (short[]) detectorIndex.get1DJavaArray(DataType.SHORT));

                readerContext.ingestToCache(bandName, productData);
            }
        }
    }
}
