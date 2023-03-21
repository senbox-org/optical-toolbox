package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.SGIDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.SGIDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class SGIDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public SGIDriverProductWriterTest() {
        super("SGI", ".rgb", "Byte", new SGIDriverProductReaderPlugIn(), new SGIDriverProductWriterPlugIn());
    }
}
