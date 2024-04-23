package eu.esa.opt.dataio.ecostress;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.dataio.geocoding.ComponentFactory;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.ForwardCoding;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import org.esa.snap.core.dataio.geocoding.GeoRaster;
import org.esa.snap.core.dataio.geocoding.InverseCoding;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.dataio.geocoding.util.RasterUtils;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.runtime.Config;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;

/**
 * Reader for ECOSTRESS L1A, L1B, L2, L3, L4 products
 *
 * @author adraghici
 */
public abstract class EcostressAbstractProductReader extends AbstractProductReader {

    /**
     * the ECOSTRESS product file object
     */
    private EcostressFile ecostressFile;

    /**
     * Constructs a new abstract product reader.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected EcostressAbstractProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * Gets the ECOSTRESS product specific metadata elements
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return the ECOSTRESS product specific metadata elements
     */
    protected abstract List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile);

    /**
     * Gets the ECOSTRESS product bands grouping pattern
     *
     * @return the ECOSTRESS product bands grouping pattern
     */
    protected abstract String getGroupingPattern();

    /**
     * Gets the ECOSTRESS product bands list
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return the ECOSTRESS product bands list
     * @throws IOException if an IO error occurs
     */
    protected abstract List<Band> getBandsList(EcostressFile ecostressFile) throws IOException;

    /**
     * Reads the ECOSTRESS product
     *
     * @return the ECOSTRESS product
     * @throws IOException if an IO error occurs
     */
    @Override
    protected Product readProductNodesImpl() throws IOException {
        final EcostressFile ecostressFile = getEcostressFile();
        final Dimension productSize = EcostressUtils.extractEcostressProductDimension(ecostressFile,EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS, EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_LINES);
        final Product product = new Product(ecostressFile.getName(), "ECOSTRESS L1B ATT ", productSize.width, productSize.height, this);
        final MetadataElement productMetadataRoot = product.getMetadataRoot();
        for (MetadataElement commonMetadataElement : getCommonMetadataElementsList(ecostressFile)) {
            productMetadataRoot.addElement(commonMetadataElement);
        }
        for (MetadataElement metadataElement : getMetadataElementsList(ecostressFile)) {
            productMetadataRoot.addElement(metadataElement);
        }
        for (Band band : getBandsList(ecostressFile)) {
            product.addBand(band);
        }
        product.setAutoGrouping(getGroupingPattern());
        product.setDescription("ECOSTRESS Product");
        product.setStartTime(EcostressUtils.extractStartTime(ecostressFile));
        product.setEndTime(EcostressUtils.extractEndTime(ecostressFile));
        product.setFileLocation(ecostressFile);
        final GeoCoding geoCoding = readPixelBasedGeoCoding(ecostressFile, product);
        if (geoCoding != null) {
            product.setSceneGeoCoding(geoCoding);
        }
        return product;
    }

    /**
     * Reads the ECOSTRESS band data subset in the provided target buffer
     *
     * @param sourceOffsetX the absolute X-offset in source raster co-ordinates
     * @param sourceOffsetY the absolute Y-offset in source raster co-ordinates
     * @param sourceWidth   the width of region providing samples to be read given in source raster co-ordinates
     * @param sourceHeight  the height of region providing samples to be read given in source raster co-ordinates
     * @param sourceStepX   the sub-sampling in X direction within the region providing samples to be read
     * @param sourceStepY   the sub-sampling in Y direction within the region providing samples to be read
     * @param targetBand    the destination band which identifies the data source from which to read the sample values
     * @param targetOffsetX the X-offset in the band's raster co-ordinates
     * @param targetOffsetY the Y-offset in the band's raster co-ordinates
     * @param targetWidth   the width of region to be read given in the band's raster co-ordinates
     * @param targetHeight  the height of region to be read given in the band's raster co-ordinates
     * @param targetBuffer  the destination buffer which receives the sample values to be read
     * @param pm            a monitor to inform the user about progress
     * @see #readBandRasterData
     * @see #getSubsetDef
     */
    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight, int sourceStepX, int sourceStepY, Band targetBand, int targetOffsetX, int targetOffsetY, int targetWidth, int targetHeight, ProductData targetBuffer, ProgressMonitor pm) {

        Assert.state(sourceOffsetX == targetOffsetX, "sourceOffsetX != targetOffsetX");
        Assert.state(sourceOffsetY == targetOffsetY, "sourceOffsetY != targetOffsetY");
        Assert.state(sourceStepX == 1, "sourceStepX != 1");
        Assert.state(sourceStepY == 1, "sourceStepY != 1");
        Assert.state(sourceWidth == targetWidth, "sourceWidth != targetWidth");
        Assert.state(sourceHeight == targetHeight, "sourceHeight != targetHeight");

        final EcostressFile ecostressFile = getEcostressFile();
        EcostressUtils.readEcostressBandData(ecostressFile, targetBand, targetWidth, targetHeight, targetOffsetX, targetOffsetY, targetBuffer);
    }

    /**
     * Gets the ECOSTRESS product file object from the reader input
     *
     * @return the ECOSTRESS product file object
     */
    protected EcostressFile getEcostressFile() {
        if (this.ecostressFile == null) {
            final Object inputObject = getInput();
            this.ecostressFile = EcostressAbstractProductReaderPlugIn.getEcostressFile(inputObject);
        }
        return this.ecostressFile;
    }

    /**
     * Gets the ECOSTRESS product common metadata from ECOSTRESS product file
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return the ECOSTRESS product common metadata
     */
    private List<MetadataElement> getCommonMetadataElementsList(EcostressFile ecostressFile) {
        return EcostressUtils.extractMetadataElements(ecostressFile, EcostressConstants.ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA);
    }

    /**
     * Reads the pixel Geocoding from ECOSTRESS product which contains 'latitude' and 'longitude' bands
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @param product the ECOSTRESS product object
     * @return the pixel Geocoding
     */
    private static GeoCoding readPixelBasedGeoCoding(EcostressFile ecostressFile, Product product) {
        final Band lonBand = findBand(product, EcostressConstants.ECOSTRESS_LONGITUDE_BAND_NAME);
        final Band latBand = findBand(product, EcostressConstants.ECOSTRESS_LATITUDE_BAND_NAME);
        if (latBand == null || lonBand == null) {
            return null;
        }

        final int width = lonBand.getRasterWidth();
        final int height = lonBand.getRasterHeight();

        final double[] longitudes = (double[]) EcostressUtils.readAndGetEcostressBandData(ecostressFile, lonBand);
        final double[] latitudes = (double[]) EcostressUtils.readAndGetEcostressBandData(ecostressFile, latBand);

        final double resolutionInKm = RasterUtils.computeResolutionInKm(longitudes, latitudes, width, height);
        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, EcostressConstants.ECOSTRESS_LONGITUDE_BAND_NAME, EcostressConstants.ECOSTRESS_LATITUDE_BAND_NAME, width, height, resolutionInKm);

        final boolean fractionalAccuracy = Config.instance().preferences().getBoolean(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, false);
        final ForwardCoding forward;
        if (fractionalAccuracy) {
            forward = ComponentFactory.getForward(PixelInterpolatingForward.KEY);
        } else {
            forward = ComponentFactory.getForward(PixelForward.KEY);
        }
        final InverseCoding inverse;
        if (fractionalAccuracy) {
            inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY_INTERPOLATING);
        } else {
            inverse = ComponentFactory.getInverse(PixelQuadTreeInverse.KEY);
        }

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.ANTIMERIDIAN);
        geoCoding.initialize();
        return geoCoding;
    }

    /**
     * Finds a band in the product by name suffix
     *
     * @param product        the product
     * @param bandNameSuffix the band name suffix
     * @return the band
     */
    private static Band findBand(Product product, String bandNameSuffix) {
        for (Band band : product.getBands()) {
            if (band.getName().endsWith(bandNameSuffix)) {
                return band;
            }
        }
        return null;
    }

}
