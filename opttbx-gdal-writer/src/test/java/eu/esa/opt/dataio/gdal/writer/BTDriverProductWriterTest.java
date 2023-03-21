package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.BTDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.BTDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class BTDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public BTDriverProductWriterTest() {
        super("BT", ".bt", "Int16 Int32 Float32", new BTDriverProductReaderPlugIn(), new BTDriverProductWriterPlugIn());
    }
}
