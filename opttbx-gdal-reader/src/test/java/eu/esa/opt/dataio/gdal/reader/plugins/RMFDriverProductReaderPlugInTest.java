package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class RMFDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public RMFDriverProductReaderPlugInTest() {
        super(".rsw", "RMF", new RMFDriverProductReaderPlugIn());
    }
}
