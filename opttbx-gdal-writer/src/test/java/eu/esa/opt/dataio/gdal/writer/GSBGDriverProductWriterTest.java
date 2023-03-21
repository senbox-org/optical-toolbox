package eu.esa.opt.dataio.gdal.writer;

import eu.esa.opt.dataio.gdal.reader.plugins.GSBGDriverProductReaderPlugIn;
import eu.esa.opt.dataio.gdal.writer.plugins.GSBGDriverProductWriterPlugIn;

/**
 * @author Jean Coravu
 */
public class GSBGDriverProductWriterTest extends AbstractTestDriverProductWriter {

    public GSBGDriverProductWriterTest() {
        super("GSBG", ".grd", "Byte Int16 UInt16 Float32", new GSBGDriverProductReaderPlugIn(), new GSBGDriverProductWriterPlugIn());
    }
}
