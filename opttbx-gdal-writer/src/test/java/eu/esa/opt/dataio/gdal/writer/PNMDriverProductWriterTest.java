package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.PNMDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.PNMDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class PNMDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public PNMDriverProductWriterTest() {
        super("PNM", ".pnm", "Byte UInt16", new PNMDriverProductReaderPlugIn(), new PNMDriverProductWriterPlugIn());
    }
}
