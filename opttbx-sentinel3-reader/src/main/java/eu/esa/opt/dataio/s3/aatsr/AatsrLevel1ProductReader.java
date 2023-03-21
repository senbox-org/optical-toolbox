package eu.esa.opt.dataio.s3.aatsr;

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import org.esa.snap.core.datamodel.Product;

import java.io.IOException;

import static eu.esa.opt.dataio.s3.aatsr.AatsrLevel1ProductReaderPlugIn.DIRECTORY_NAME_PATTERN;

/**
 * @author Sabine Embacher
 */
public class AatsrLevel1ProductReader extends Sentinel3ProductReader {

    public AatsrLevel1ProductReader(AatsrLevel1ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final String dirName = getInputFileParentDirectory().getName();
        if (dirName.matches(DIRECTORY_NAME_PATTERN)) {
            setFactory(new AatsrLevel1ProductFactory(this));
        }

        Product product = createProduct();
        product.setProductType(dirName.substring(0, 12));

        return product;
    }

}
