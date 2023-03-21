package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class GTXDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public GTXDriverProductReaderPlugInTest() {
        super(".gtx", "GTX", new GTXDriverProductReaderPlugIn());
    }
}
