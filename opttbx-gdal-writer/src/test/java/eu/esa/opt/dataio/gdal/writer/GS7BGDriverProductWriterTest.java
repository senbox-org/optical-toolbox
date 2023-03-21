package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.GS7BGDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.GS7BGDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class GS7BGDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public GS7BGDriverProductWriterTest() {
        super("GS7BG", ".grd", "Byte Int16 UInt16 Float32 Float64", new GS7BGDriverProductReaderPlugIn(), new GS7BGDriverProductWriterPlugIn());
    }
}
