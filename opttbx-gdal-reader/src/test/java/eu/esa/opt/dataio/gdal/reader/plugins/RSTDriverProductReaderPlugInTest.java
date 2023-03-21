package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class RSTDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public RSTDriverProductReaderPlugInTest() {
        super(".rst", "RST", new RSTDriverProductReaderPlugIn());
    }
}
