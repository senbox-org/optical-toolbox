package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.Manifest;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.slstr.dddb.SlstrDDDB;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SlstrFrpEumProductFactory extends SlstrProductFactory {

    // @todo 1 check if we need this - possibly it is a good concept
    //
    // CheckDecodeQualification
    // ------------------------
    // - open "manifest.xml"
    // - check all files present - compare with DDDB

    public SlstrFrpEumProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    public Product createProduct(VirtualDir virtualDir) throws IOException {
        final InputStream manifestInputStream = getManifestInputStream(virtualDir);
        final Manifest manifest = createManifest(manifestInputStream);

        final MetadataElement metadataRoot = manifest.getMetadata();
        final String productType = manifest.getProductType();
        final String productName = manifest.getProductName();
        final Product product = new Product(productName, productType);

        String version = manifest.getProcessingVersion();

        final SlstrDDDB dddb = SlstrDDDB.instance();
        //dddb.getVariables(productType, version);

        return product;
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected Double getStartOffset(String gridIndex) {
        throw new RuntimeException("not implemented");
    }

    @Override
    protected Double getTrackOffset(String gridIndex) {
        throw new RuntimeException("not implemented");
    }

    Dimension getProductSize() {
        return new Dimension(20, 40);
    }
}
