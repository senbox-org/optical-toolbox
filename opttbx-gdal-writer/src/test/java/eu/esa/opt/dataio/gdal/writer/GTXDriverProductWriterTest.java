package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.GTXDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.GTXDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class GTXDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public GTXDriverProductWriterTest() {
        super("GTX", ".gtx", "Float32", new GTXDriverProductReaderPlugIn(), new GTXDriverProductWriterPlugIn());
    }
}
