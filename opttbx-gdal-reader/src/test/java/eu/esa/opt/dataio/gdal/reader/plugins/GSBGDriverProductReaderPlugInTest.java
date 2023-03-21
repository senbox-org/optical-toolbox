package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class GSBGDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public GSBGDriverProductReaderPlugInTest() {
        super(".grd", "GSBG", new GSBGDriverProductReaderPlugIn());
    }
}
