package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

import java.util.List;

public class SlstrAodProductFactory extends SlstrProductFactory {

    public SlstrAodProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    protected Double getStartOffset(String gridIndex) {
        return 0.0;
    }

    @Override
    protected Double getTrackOffset(String gridIndex) {
        return 0.0;
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        return manifest.getFileNames("");
    }

    @Override
    protected boolean isNodeSpecial(Band sourceBand, Product targetProduct) {
        return false;
    }
}
