package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.NITFDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.NITFDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class NITFDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public NITFDriverProductWriterTest() {
        super("NITF", ".ntf", "Byte UInt16 Int16 UInt32 Int32 Float32", new NITFDriverProductReaderPlugIn(), new NITFDriverProductWriterPlugIn());
    }
}
