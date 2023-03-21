package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class GS7BGDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public GS7BGDriverProductReaderPlugInTest() {
        super(".grd", "GS7BG", new GS7BGDriverProductReaderPlugIn());
    }
}
