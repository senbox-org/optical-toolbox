package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class SAGADriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public SAGADriverProductReaderPlugInTest() {
        super(".sdat", "SAGA", new SAGADriverProductReaderPlugIn());
    }
}
