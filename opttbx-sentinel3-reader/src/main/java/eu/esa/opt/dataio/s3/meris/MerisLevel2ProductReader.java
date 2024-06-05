package eu.esa.opt.dataio.s3.meris;

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Product;

import java.io.File;
import java.io.IOException;

/**
 * @author Tonio Fincke
 */
public class MerisLevel2ProductReader extends Sentinel3ProductReader {

    public MerisLevel2ProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        ensureVirtualDir(inputFile);

        final File baseFile = getVirtualDir().getBaseFile();
        final String baseFileName = baseFile.getName();
        if (baseFileName.matches("ENV_ME_2_(F|R)R(G|P).*.SEN3")) {
            setFactory(new MerisLevel2ProductFactory(this));
        }
        return createProduct();
    }
}
