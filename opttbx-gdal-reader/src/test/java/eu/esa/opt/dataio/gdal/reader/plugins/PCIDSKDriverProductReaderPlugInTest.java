package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class PCIDSKDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public PCIDSKDriverProductReaderPlugInTest() {
        super(".pix", "PCIDSK", new PCIDSKDriverProductReaderPlugIn());
    }
}
