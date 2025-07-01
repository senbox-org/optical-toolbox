package eu.esa.opt.dataio.s3.slstr;

import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;

import java.io.IOException;
import java.util.List;

public class SlstrAodProductFactory extends SlstrProductFactory {

    private static final double RESOLUTION_IN_KM = 9.5;

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
        targetProduct.setAutoGrouping("AOD:SSA");
    }
}
