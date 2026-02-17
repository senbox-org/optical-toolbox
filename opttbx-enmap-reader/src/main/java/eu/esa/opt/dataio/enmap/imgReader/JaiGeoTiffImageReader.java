package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFRenderedImage;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.geotiff.GeoTiffImageReader;

import javax.media.jai.operator.BandSelectDescriptor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.getInputStream;


class JaiGeoTiffImageReader extends EnmapImageReader {


    private GeoTiffImageReader geoTiffImageReader;


    private JaiGeoTiffImageReader(GeoTiffImageReader geoTiffImageReader) {
        this.geoTiffImageReader = geoTiffImageReader;
    }

    public static EnmapImageReader createImageReader(VirtualDir dataDir, String fileName, boolean isNonCompliantProduct) throws IOException {
        try {
            return new JaiGeoTiffImageReader(new GeoTiffImageReader(getInputStream(dataDir, fileName, isNonCompliantProduct), () -> {
            }));
        } catch (IllegalStateException ise) {
            throw new IOException("Could not create spectral data reader.", ise);
        }
    }

    @Override
    public Dimension getTileDimension() throws IOException {
        TIFFRenderedImage baseImage = geoTiffImageReader.getBaseImage();
        return new Dimension(baseImage.getTileWidth(), baseImage.getTileHeight());
    }

    @Override
    public int getNumImages() throws IOException {
        return geoTiffImageReader.getBaseImage().getSampleModel().getNumBands();
    }

    @Override
    public RenderedImage getImageAt(int index) throws IOException {
        return BandSelectDescriptor.create(geoTiffImageReader.getBaseImage(), new int[]{index}, null);
    }

    @Override
    public void readLayerBlock(int startLayer, int numLayers, int x, int y, int width, int height, ProductData targetData) throws IOException {
        final Rectangle area = new Rectangle(x, y, width, height);
        final Raster data = geoTiffImageReader.getBaseImage().getData(area);
        int targetIndex = 0;
        for (int layer = 0; layer < numLayers; layer++) {
            final int[] samples = data.getSamples(x, y, width, height, startLayer + layer, (int[]) null);
            for (int sample : samples) {
                targetData.setElemIntAt(targetIndex++, sample);
            }
        }
    }

    @Override
    public boolean isInterleavedReadOptimized() {
        return true;
    }

    @Override
    public void close() {
        geoTiffImageReader.close();
    }
}
