package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import it.geosolutions.imageioimpl.plugins.tiff.TIFFRenderedImage;
import org.esa.snap.dataio.geotiff.GeoTiffImageReader;

import javax.media.jai.operator.BandSelectDescriptor;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.io.IOException;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.getInputStream;

class JaiGeoTiffImageReader implements EnmapImageReader {
    private GeoTiffImageReader geoTiffImageReader;

    private JaiGeoTiffImageReader(GeoTiffImageReader geoTiffImageReader) {
        this.geoTiffImageReader = geoTiffImageReader;
    }

    public static EnmapImageReader createImageReader(VirtualDir dataDir, String fileName) throws IOException {
        try {
            return new JaiGeoTiffImageReader(new GeoTiffImageReader(getInputStream(dataDir, fileName), () -> {
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
    public void close() {
        geoTiffImageReader.close();
    }
}
