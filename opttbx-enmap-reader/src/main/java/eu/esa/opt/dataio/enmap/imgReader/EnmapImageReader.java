package eu.esa.opt.dataio.enmap.imgReader;

import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.enmap.EnmapMetadata;
import eu.esa.opt.dataio.enmap.ProductFormat;
import org.esa.snap.runtime.Config;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Map;
import java.util.prefs.Preferences;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.*;

public abstract class EnmapImageReader {

    private static String ENMAP_GEOTIFF_USE_JAI = "enmap.geotiff.useJai";

    // todo - should be private but this is only possible with Java 9
    public static EnmapImageReader createSpectralReader(VirtualDir dataDir, EnmapMetadata meta) throws IOException {
        return getEnmapImageReader(meta, dataDir, SPECTRAL_IMAGE_VNIR_KEY, SPECTRAL_IMAGE_SWIR_KEY, SPECTRAL_IMAGE_KEY);
    }

    // todo - should be private but this is only possible with Java 9
    public static EnmapImageReader createPixelMaskReader(VirtualDir dataDir, EnmapMetadata meta) throws IOException {
        return getEnmapImageReader(meta, dataDir, QUALITY_PIXELMASK_VNIR_KEY, QUALITY_PIXELMASK_SWIR_KEY, QUALITY_PIXELMASK_KEY);
    }

    // todo - should be private but this is only possible with Java 9
    static EnmapImageReader getEnmapImageReader(EnmapMetadata meta, VirtualDir dataDir, String vnirKey, String swirKey, String orthoKey) throws IOException {
        switch (meta.getProcessingLevel()) {
            case L1B:
                return new L1BSpectrumImageReader(dataDir, meta, vnirKey, swirKey);
            case L1C:
            case L2A:
                return createImageReader(dataDir, meta, orthoKey);
            default:
                throw new IOException(String.format("Unknown product level '%s'", meta.getProcessingLevel()));
        }
    }

    /**
     * Creates an image reader. Implementation depends on the format of the data retrieved from the metadata.
     * Currently only {@link ProductFormat GeoTIFF+Metadata} is supported.
     *
     * @param dataDir  the directory where the data is located
     * @param meta     the metadata of the EnMAP product
     * @param imageKey The key indicating which data shall be read
     * @return reader instance to access the image data
     * @throws IOException in case an exception occurs
     */
    public static EnmapImageReader createImageReader(VirtualDir dataDir, EnmapMetadata meta, String imageKey) throws IOException {
        final Preferences preferences = Config.instance("enmap").load().preferences();
        // based on format we decide which reader to use; currently only GeoTiff supported
        String productFormat = meta.getProductFormat();
        ProductFormat format = ProductFormat.valueOf(ProductFormat.toEnumName(productFormat));
        if (ProductFormat.GeoTIFF_Metadata == format) {
            boolean useJai = preferences.getBoolean(ENMAP_GEOTIFF_USE_JAI, false);
            Map<String, String> fileNameMap = meta.getFileNameMap();
            if (useJai) {
                return JaiGeoTiffImageReader.createImageReader(dataDir, fileNameMap.get(imageKey), meta.isNonCompliantProduct());
            } else {
                return GdalGeoTiffImageReader.createImageReader(dataDir, fileNameMap.get(imageKey), meta.isNonCompliantProduct());

            }
        } else {
            throw new IllegalStateException(String.format("The product format '%s' is not supported", productFormat));
        }

    }


    /**
     * returns the dimension of the image tiles
     *
     * @return the tile dimension
     * @throws IOException in case the information could not be retrieved from the source
     */
    abstract public Dimension getTileDimension() throws IOException;

    /**
     * returns the number of images provided by this reader
     *
     * @return the number of images
     * @throws IOException in case the information could not be retrieved from the source
     */
    abstract public int getNumImages() throws IOException;

    /**
     * returns the spectral image at the specified index (zero based)
     *
     * @param index the spectral index
     * @return the image at the given spectral index
     * @throws IOException              in case the information could not be retrieved from the source
     * @throws IllegalArgumentException in case the index is less than zero or higher than the maximum number of images minus one
     */
    abstract public RenderedImage getImageAt(int index) throws IOException;

    /**
     * Closes any open resource
     */
    abstract public void close();
}
