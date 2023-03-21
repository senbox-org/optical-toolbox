package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.BMPDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.BMPDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class BMPDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public BMPDriverProductWriterTest() {
        super("BMP", ".bmp", "Byte", new BMPDriverProductReaderPlugIn(), new BMPDriverProductWriterPlugIn());
    }
}
