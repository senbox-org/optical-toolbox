package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.KRODriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.KRODriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class KRODriverProductWriterTest extends AbstractTestDriverProductWriter {

    public KRODriverProductWriterTest() {
        super("KRO", ".kro", "Byte UInt16 Float32", new KRODriverProductReaderPlugIn(), new KRODriverProductWriterPlugIn());
    }
}
