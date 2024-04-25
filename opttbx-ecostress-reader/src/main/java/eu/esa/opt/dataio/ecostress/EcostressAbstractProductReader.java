package eu.esa.opt.dataio.ecostress;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;
import org.apache.http.auth.Credentials;
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
import org.esa.snap.core.datamodel.CrsGeoCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.product.library.v2.preferences.RepositoriesCredentialsController;
import org.esa.snap.product.library.v2.preferences.model.RemoteRepositoryCredentials;
import org.esa.snap.remote.products.repository.RepositoryProduct;
import org.esa.snap.remote.products.repository.download.RemoteRepositoriesManager;
import org.esa.snap.remote.products.repository.download.RemoteRepositoryProductImpl;
import org.esa.snap.remote.products.repository.geometry.Polygon2D;
import org.esa.snap.runtime.Config;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;

/**
 * Reader for ECOSTRESS L1A, L1B, L2, L3, L4 products
 *
 * @author adraghici
 */
public abstract class EcostressAbstractProductReader extends AbstractProductReader {

    private static final Logger logger = Logger.getLogger(EcostressAbstractProductReader.class.getName());

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
     * Gets the remote platform name
     *
     * @return the remote platform name
     */
    protected abstract String getRemotePlatformName();

    /**
     * Reads the ECOSTRESS product
     *
     * @return the ECOSTRESS product
     * @throws IOException if an IO error occurs
     */
    @Override
    protected Product readProductNodesImpl() throws IOException {
        final EcostressFile ecostressFile = getEcostressFile();
        final List<Band> bandsList = getBandsList(ecostressFile);
        Dimension productSize = EcostressUtils.extractEcostressProductDimension(ecostressFile, EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS, EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_LINES);
        if (productSize.height < 1 || productSize.width < 1) {
            productSize = computeProductDimensionUsingBandsDimensions(bandsList);
        }
        final Product product = new Product(ecostressFile.getName(), "ECOSTRESS L1B ATT ", productSize.width, productSize.height, this);
        final MetadataElement productMetadataRoot = product.getMetadataRoot();
        for (MetadataElement commonMetadataElement : getCommonMetadataElementsList(ecostressFile)) {
            productMetadataRoot.addElement(commonMetadataElement);
        }
        for (MetadataElement metadataElement : getMetadataElementsList(ecostressFile)) {
            productMetadataRoot.addElement(metadataElement);
        }
        for (Band band : bandsList) {
            product.addBand(band);
        }
        product.setAutoGrouping(getGroupingPattern());
        product.setDescription("ECOSTRESS Product");
        product.setStartTime(EcostressUtils.extractStartTime(ecostressFile));
        product.setEndTime(EcostressUtils.extractEndTime(ecostressFile));
        product.setFileLocation(ecostressFile);
        final GeoCoding geoCoding = buildGeoCoding(ecostressFile, product);
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

    private GeoCoding buildGeoCoding(EcostressFile ecostressFile, Product product) {
        GeoCoding productGeoCoding = readPixelBasedGeoCoding(ecostressFile, product);
        if (productGeoCoding != null) {
            return productGeoCoding;
        }
        productGeoCoding = buildCrsGeoCodingUsingProductMetadata(product);
        if (productGeoCoding != null) {
            return productGeoCoding;
        }
        final String remotePlatformName = getRemotePlatformName();
        return buildCrsGeoCodingUsingRemoteRepository(product, remotePlatformName);
    }

    /**
     * Computes the product dimension by choosing the size of the biggest band
     *
     * @param bandList the product bands list
     * @return the computed product dimension
     */
    private static Dimension computeProductDimensionUsingBandsDimensions(List<Band> bandList) {
        final Dimension productDimension = new Dimension(0, 0);
        for (Band band : bandList) {
            final Dimension bandDimension = band.getRasterSize();
            if (bandDimension.width > productDimension.width || bandDimension.height > productDimension.height) {
                productDimension.setSize(bandDimension);
            }
        }
        return productDimension;
    }

    /**
     * Reads the pixel Geocoding from ECOSTRESS product which contains 'latitude' and 'longitude' bands
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @param product       the ECOSTRESS product object
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
     * Builds the CrsGeoCoding using the geographic coordinates information from the product metadata.
     *
     * @param product the ECOSTRESS product object
     * @return the CrsGeoCoding
     */
    private static GeoCoding buildCrsGeoCodingUsingProductMetadata(Product product) {
        final MetadataElement productMetadata = product.getMetadataRoot().getElementAt(0);
        final double topLeftLon = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_WEST_BOUNDING_COORDINATE);
        final double topRightLon = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_EAST_BOUNDING_COORDINATE);
        final double topLeftLat = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_NORTH_BOUNDING_COORDINATE);
        final double bottomLeftLat = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_SOUTH_BOUNDING_COORDINATE);
        return buildCrsGeoCoding(product, topLeftLon, topRightLon, topLeftLat, bottomLeftLat);
    }

    private static GeoCoding buildCrsGeoCodingUsingRemoteRepository(Product product, String remotePlatformName) {
        final String productName = product.getName();
        final RepositoriesCredentialsController repositoriesCredentialsController = RepositoriesCredentialsController.getInstance();
        final RemoteRepositoryCredentials remoteRepositoryCredentials = repositoriesCredentialsController.getRepositoriesCredentials().stream().filter(rrc -> rrc.getRepositoryName().equals(EcostressConstants.REMOTE_REPOSITORY_NAME)).findFirst().orElse(null);
        if (remoteRepositoryCredentials != null && !remoteRepositoryCredentials.getCredentialsList().isEmpty()) {
            final Credentials credentials = remoteRepositoryCredentials.getCredentialsList().get(0);
            final Map<String, Object> parameterValues = new LinkedHashMap<>();
            parameterValues.put("platformName", remotePlatformName);
            parameterValues.put("productIdentifier", productName.replaceAll("ECOSTRESS_(.*?_.*?_.*?_.*?)_.*", "$1"));
            try {
                final List<RepositoryProduct> remoteProductsList = RemoteRepositoriesManager.downloadProductList(EcostressConstants.REMOTE_REPOSITORY_NAME, EcostressConstants.REMOTE_MISSION_NAME, 1, credentials, parameterValues, null, null);
                if (!remoteProductsList.isEmpty()) {
                    final RemoteRepositoryProductImpl remoteProduct = (RemoteRepositoryProductImpl) remoteProductsList.get(0);
                    if (remoteProduct.getName().equals(productName)) {
                        final Polygon2D remoteProductCoordinates = (Polygon2D) remoteProduct.getPolygon();
                        if (remoteProductCoordinates != null && remoteProductCoordinates.getPathCount() > 0) {
                            final List<Point2D> coordinatePoints=extractPolygon2DCoordinates(remoteProductCoordinates);
                            final double topLeftLon = coordinatePoints.get(0).getX();
                            final double topRightLon = coordinatePoints.get(1).getX();
                            final double topLeftLat = coordinatePoints.get(2).getY();
                            final double bottomLeftLat = coordinatePoints.get(0).getY();
                            return buildCrsGeoCoding(product, topLeftLon, topRightLon, topLeftLat, bottomLeftLat);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warning("Cannot build geocoding using remote repository for product '" + product.getName() + "': " + e.getMessage());
            }
        }
        return null;
    }

    private static List<Point2D> extractPolygon2DCoordinates(Polygon2D polygon2D) {
        final List<Point2D> coordinatesPoints = new ArrayList<>();
        final PathIterator pathIterator = polygon2D.getPath().getPathIterator(null);
        final double[] coordinates = new double[2];
        while (!pathIterator.isDone()) {
            pathIterator.currentSegment(coordinates);
            pathIterator.next();
            final Point2D coordinatePoint = new Point2D.Double(coordinates[0], coordinates[1]);
            coordinatesPoints.add(coordinatePoint);
        }
        return coordinatesPoints;
    }

    /**
     * Builds the CrsGeoCoding using the provided geographic coordinates information.
     *
     * @param product       the ECOSTRESS product object
     * @param topLeftLon    the top left longitude coordinate
     * @param topRightLon   the top right longitude coordinate
     * @param topLeftLat    the top left latitude coordinate
     * @param bottomLeftLat the bottom left latitude coordinate
     * @return the CrsGeoCoding
     */
    private static GeoCoding buildCrsGeoCoding(Product product, double topLeftLon, double topRightLon, double topLeftLat, double bottomLeftLat) {
        final int productWidth = product.getSceneRasterWidth();
        final int productHeight = product.getSceneRasterHeight();
        final double pixelSizeX = Math.abs(topRightLon - topLeftLon) / (productWidth - 1);
        final double pixelSizeY = (topLeftLat - bottomLeftLat) / (productHeight - 1);

        try {
            return new CrsGeoCoding(DefaultGeographicCRS.WGS84, productWidth, productHeight, topLeftLon, topLeftLat, pixelSizeX, pixelSizeY);
        } catch (Exception e) {
            logger.warning("Cannot build CRS geocoding for product '" + product.getName() + "': " + e.getMessage());
        }
        return null;
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
