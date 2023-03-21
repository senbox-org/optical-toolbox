package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class BTDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public BTDriverProductReaderPlugInTest() {
        super(".bt", "BT", new BTDriverProductReaderPlugIn());
    }
}
