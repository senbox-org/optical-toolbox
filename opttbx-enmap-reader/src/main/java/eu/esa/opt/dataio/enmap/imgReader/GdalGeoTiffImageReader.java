package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Product;

import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.getRelativePath;

class GdalGeoTiffImageReader extends EnmapImageReader {
    public static final String GDAL_FORMAT_NAME = "GDAL-GTiff-READER";
    private final Product product;

    private GdalGeoTiffImageReader(Product gtProduct) {
        this.product = gtProduct;
    }

    public static EnmapImageReader createImageReader(VirtualDir dataDir, String fileName) throws IOException {
        try {
            Iterator<ProductReaderPlugIn> readerPlugIns =
                    ProductIOPlugInManager.getInstance().getReaderPlugIns(GDAL_FORMAT_NAME);
            if (readerPlugIns.hasNext()) {
                ProductReader reader = readerPlugIns.next().createReaderInstance();
                String relativePath = getRelativePath(dataDir, fileName);
                Product product = reader.readProductNodes(dataDir.getFile(relativePath), null);
                return new GdalGeoTiffImageReader(product);
            } else {
                throw new IllegalStateException(String.format("Reader '%s' not found.", GDAL_FORMAT_NAME));
            }
        } catch (IllegalStateException ise) {
            throw new IOException("Could not create data reader.", ise);
        }
    }

    @Override
    public Dimension getTileDimension() {
        return product.getSceneRasterSize();
    }

    @Override
    public int getNumImages() {
        return product.getNumBands();
    }

    @Override
    public RenderedImage getImageAt(int index) {
        return product.getBandAt(index).getSourceImage();
    }

    @Override
    public void close() {
        product.dispose();
    }
}
