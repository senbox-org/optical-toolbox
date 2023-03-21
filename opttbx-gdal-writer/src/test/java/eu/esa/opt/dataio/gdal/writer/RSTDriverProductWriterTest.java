package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.RSTDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.RSTDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class RSTDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public RSTDriverProductWriterTest() {
        super("RST", ".rst", "Byte Int16 Float32", new RSTDriverProductReaderPlugIn(), new RSTDriverProductWriterPlugIn());
    }
}
