package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class MFFDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public MFFDriverProductReaderPlugInTest() {
        super(".hdr", "MFF", new MFFDriverProductReaderPlugIn());
    }
}
