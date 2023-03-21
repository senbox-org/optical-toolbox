package eu.esa.opt.dataio.gdal.reader.plugins;

/**
 * @author Jean Coravu
 */
public class ILWISDriverProductReaderPlugInTest extends AbstractTestDriverProductReaderPlugIn {

    public ILWISDriverProductReaderPlugInTest() {
        super("ILWIS", new ILWISDriverProductReaderPlugIn());

        addExtensin(".mpr");
        addExtensin(".mpl");
    }
}
