package eu.esa.opt.dataio.gdal.writer.plugins;

/**
 * @author Jean Coravu
 */
public class PNMDriverProductWriterPlugInTest extends AbstractTestDriverProductWriterPlugIn {

    public PNMDriverProductWriterPlugInTest() {
        super("PNM", new PNMDriverProductWriterPlugIn());
    }
}
