package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import org.esa.snap.core.dataio.ProductIOPlugInManager;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.getRelativePath;


class GdalGeoTiffImageReader extends EnmapImageReader {


    private static final int MAX_CACHE_TILE_SIZE = 256;
    public static final String GDAL_FORMAT_NAME = "GDAL-GTiff-READER";
    private final Product product;


    private GdalGeoTiffImageReader(Product gtProduct) {
        this.product = gtProduct;
    }


    public static EnmapImageReader createImageReader(VirtualDir dataDir, String fileName, boolean isNonCompliantProduct) throws IOException {
        try {
            Iterator<ProductReaderPlugIn> readerPlugIns =
                    ProductIOPlugInManager.getInstance().getReaderPlugIns(GDAL_FORMAT_NAME);
            if (readerPlugIns.hasNext()) {
                ProductReader reader = readerPlugIns.next().createReaderInstance();
                String relativePath = getRelativePath(dataDir, fileName, isNonCompliantProduct);
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
        final RenderedImage sourceImage = product.getBandAt(0).getSourceImage();
        final int sceneWidth = product.getSceneRasterWidth();
        final int sceneHeight = product.getSceneRasterHeight();
        final int tileWidth = normalizeTileSize(sourceImage.getTileWidth(), sceneWidth);
        final int tileHeight = normalizeTileSize(sourceImage.getTileHeight(), sceneHeight);
        return new Dimension(tileWidth, tileHeight);
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
    public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) {
        int targetIndex = 0;
        final Rectangle area = new Rectangle(x, y, width, height);
        for (int layer = 0; layer < numLayers; layer++) {
            final RenderedImage image = getImageAt(startLayer + layer);
            final Raster data = image.getData(area);
            final int[] samples = data.getSamples(x, y, width, height, 0, (int[]) null);
            for (int sample : samples) {
                targetData.setElemIntAt(targetIndex++, sample);
            }
        }
    }

    @Override
    public boolean isInterleavedReadOptimized() {
        return false;
    }

    @Override
    public void close() {
        product.dispose();
    }

    static int normalizeTileSize(int sourceTileSize, int sceneSize) {
        if (sceneSize <= 0) {
            return 1;
        }
        final int upperBound = Math.min(MAX_CACHE_TILE_SIZE, sceneSize);
        if (sourceTileSize <= 1 || sourceTileSize >= sceneSize) {
            return upperBound;
        }
        return Math.min(sourceTileSize, upperBound);
    }
}
