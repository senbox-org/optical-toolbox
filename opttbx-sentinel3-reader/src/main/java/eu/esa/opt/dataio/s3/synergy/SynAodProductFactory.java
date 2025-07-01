package eu.esa.opt.dataio.s3.synergy;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.s3.AbstractProductFactory;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.util.S3NetcdfReader;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static eu.esa.opt.dataio.s3.olci.OlciProductFactory.getFileFromVirtualDir;

public class SynAodProductFactory extends AbstractProductFactory {

    private static final double FILL_VALUE = -999.0;
    private static final double RESOLUTION_IN_KM = 4.5;

    private final static String SYSPROP_SYN_AOD_PIXEL_GEO_CODING_INVERSE = "opttbx.reader.syn.aod.pixelGeoCoding.inverse";

    public SynAodProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        return manifest.getFileNames(new String[0]);
    }

    @Override
    protected Product readProduct(String fileName, Manifest manifest, VirtualDir virtualDir) throws IOException {
        final File file = getFileFromVirtualDir(fileName, virtualDir);
        if (!file.exists()) {
            return null;
        }
        return new S3NetcdfReader().readProductNodes(file, null);
    }

    @Override
    protected void setGeoCoding(Product targetProduct) throws IOException {
        final Band lonBand = targetProduct.getBand("longitude");
        final Band latBand = targetProduct.getBand("latitude");
        if (lonBand == null || latBand == null) {
            return;
        }
        ComponentGeoCoding geoCoding =
                GeoCodingFactory.createPixelGeoCoding(latBand, lonBand, RESOLUTION_IN_KM);
        targetProduct.setSceneGeoCoding(geoCoding);
    }

    @Override
    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        targetProduct.setAutoGrouping("AOD:SSA:Surface_reflectance");
    }
}
