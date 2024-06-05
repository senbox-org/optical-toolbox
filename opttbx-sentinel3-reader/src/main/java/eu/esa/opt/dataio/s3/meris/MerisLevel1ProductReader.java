package eu.esa.opt.dataio.s3.meris;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import org.esa.snap.core.datamodel.Product;

import java.io.File;
import java.io.IOException;

/**
 * @author Tonio Fincke
 */
public class MerisLevel1ProductReader extends Sentinel3ProductReader {

    public MerisLevel1ProductReader(MerisLevel1ProductPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        final File inputFile = getInputFile();
        ensureVirtualDir(inputFile);
        final VirtualDir virtualDir = getVirtualDir();

        File baseFile = virtualDir.getBaseFile();
        String baseFileName = baseFile.getName();
        if (baseFileName.matches("EN.*_(F|R)R(G|P|S).*")) {
            setFactory(new MerisLevel1ProductFactory(this));
        }
        return createProduct();
    }

}
