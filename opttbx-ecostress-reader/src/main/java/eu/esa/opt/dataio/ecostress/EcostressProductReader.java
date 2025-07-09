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
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.util.ArrayUtils;
import org.esa.snap.core.util.GeoUtils;
import org.esa.snap.product.library.v2.preferences.RepositoriesCredentialsController;
import org.esa.snap.product.library.v2.preferences.model.RemoteRepositoryCredentials;
import org.esa.snap.remote.products.repository.RepositoryProduct;
import org.esa.snap.remote.products.repository.download.RemoteRepositoriesManager;
import org.esa.snap.remote.products.repository.download.RemoteRepositoryProductImpl;
import org.esa.snap.remote.products.repository.geometry.Polygon2D;
import org.esa.snap.runtime.Config;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
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
public class EcostressProductReader extends AbstractProductReader {

    private static final Logger logger = Logger.getLogger(EcostressProductReader.class.getName());
    private EcostressFile ecostressFile;

    /**
     * Constructs a new product reader for reading the ECOSTRESS products.
     *
     * @param readerPlugIn the reader plug-in which created this reader, can be {@code null} for internal reader
     *                     implementations
     */
    protected EcostressProductReader(ProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
    }

    /**
     * Gets the ECOSTRESS product specific metadata elements
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return the ECOSTRESS product specific metadata elements
     */
    protected List<MetadataElement> getMetadataElementsList(EcostressFile ecostressFile, EcostressMetadata ecostressMetadata) {
        return EcostressUtils.extractMetadataElements(ecostressFile, ecostressMetadata.getMetadataElementsPaths());
    }

    /**
     * Gets the ECOSTRESS product bands list
     *
     * @param ecostressFile the ECOSTRESS product file object
     * @return the ECOSTRESS product bands list
     */
    protected List<Band> getBandsList(EcostressFile ecostressFile, EcostressMetadata ecostressMetadata) {
        return EcostressUtils.extractBandsObjects(ecostressFile, ecostressMetadata.getBandsElementsPaths());
    }

    /**
     * Reads the ECOSTRESS product
     *
     * @return the ECOSTRESS product
     */
    @Override
    protected Product readProductNodesImpl() {
        final EcostressFile ecostressFile = getEcostressFile();
        final EcostressMetadata ecostressMetadata = EcostressMetadata.getEcostressMetadataForFile(ecostressFile);
        if (ecostressMetadata == null) {
            return null;
        }
        String pathOfGeneralMetadata = EcostressConstants.ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA;
        for (String metadataElementPath : ecostressMetadata.getMetadataElementsPaths()) {
            if (metadataElementPath.endsWith(EcostressConstants.ECOSTRESS_PRODUCT_DATA_DEFINITIONS_GROUP_STANDARD_METADATA)) {
                pathOfGeneralMetadata = metadataElementPath;
            }
        }
        final List<Band> bandsList = getBandsList(ecostressFile, ecostressMetadata);
        Dimension productSize = EcostressUtils.extractEcostressProductDimension(ecostressFile, pathOfGeneralMetadata + EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_PIXELS, pathOfGeneralMetadata + EcostressConstants.ECOSTRESS_STANDARD_METADATA_IMAGE_LINES);
        if (productSize.height < 1 || productSize.width < 1) {
            productSize = computeProductDimensionUsingBandsDimensions(bandsList);
        }
        final EcostressProduct ecostressProduct = new EcostressProduct(ecostressFile.getName(), ecostressMetadata.getFormatName(), productSize.width, productSize.height, this);
        final MetadataElement productMetadataRoot = ecostressProduct.getMetadataRoot();
        for (MetadataElement metadataElement : getMetadataElementsList(ecostressFile, ecostressMetadata)) {
            productMetadataRoot.addElement(metadataElement);
            final MetadataAttribute dayNightFlagAttribute = metadataElement.getAttribute(EcostressConstants.ECOSTRESS_STANDARD_METADATA_DAY_NIGHT_FLAG);
            if (dayNightFlagAttribute != null) {
                final boolean isNight = dayNightFlagAttribute.getData().getElemString().equalsIgnoreCase("night");
                ecostressProduct.setReversed(ecostressMetadata.isReversedOnNight() && isNight);
            }
        }
        for (Band band : bandsList) {
            ecostressProduct.addBand(band);
        }
        ecostressProduct.setAutoGrouping(ecostressMetadata.getGroupingPattern());
        ecostressProduct.setDescription("ECOSTRESS Product");
        ecostressProduct.setStartTime(EcostressUtils.extractStartTime(ecostressFile, pathOfGeneralMetadata));
        ecostressProduct.setEndTime(EcostressUtils.extractEndTime(ecostressFile, pathOfGeneralMetadata));
        ecostressProduct.setFileLocation(ecostressFile);
        final GeoCoding geoCoding = buildGeoCoding(ecostressFile, ecostressProduct, ecostressMetadata);
        if (geoCoding != null) {
            ecostressProduct.setSceneGeoCoding(geoCoding);
        }
        return ecostressProduct;
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
        final boolean isBandRasterReversed = isBandRasterReversed(targetBand);
        EcostressUtils.readEcostressBandData(ecostressFile, targetBand, targetWidth, targetHeight, targetOffsetX, targetOffsetY, targetBuffer, isBandRasterReversed);
    }

    /**
     * Gets the ECOSTRESS product file object from the reader input
     *
     * @return the ECOSTRESS product file object
     */
    protected EcostressFile getEcostressFile() {
        if (this.ecostressFile == null) {
            final Object inputObject = getInput();
            this.ecostressFile = EcostressProductReaderPlugIn.getEcostressFile(inputObject);
        }
        return this.ecostressFile;
    }

    private GeoCoding buildGeoCoding(EcostressFile ecostressFile, EcostressProduct ecostressProduct, EcostressMetadata ecostressMetadata) {
        GeoCoding productGeoCoding = readPixelBasedGeoCoding(ecostressFile, ecostressProduct);
        if (productGeoCoding != null) {
            return productGeoCoding;
        }
        productGeoCoding = buildCrsGeoCodingUsingProductMetadata(ecostressProduct);
        if (productGeoCoding != null) {
            return productGeoCoding;
        }
        final String remotePlatformName = ecostressMetadata.getRemotePlatformName();
        if (remotePlatformName == null) {
            return null;
        }
        return buildCrsGeoCodingUsingRemoteRepository(ecostressProduct, remotePlatformName);
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
     * @param ecostressProduct       the ECOSTRESS product object
     * @return the pixel Geocoding
     */
    private static GeoCoding readPixelBasedGeoCoding(EcostressFile ecostressFile, EcostressProduct ecostressProduct) {
        final Band lonBand = findBand(ecostressProduct, EcostressConstants.ECOSTRESS_LONGITUDE_BAND_NAME);
        final Band latBand = findBand(ecostressProduct, EcostressConstants.ECOSTRESS_LATITUDE_BAND_NAME);
        if (latBand == null || lonBand == null) {
            return null;
        }

        final int width = lonBand.getRasterWidth();
        final int height = lonBand.getRasterHeight();

        final double[] longitudes = (double[]) EcostressUtils.readAndGetEcostressBandData(ecostressFile, lonBand);
        final double[] latitudes = (double[]) EcostressUtils.readAndGetEcostressBandData(ecostressFile, latBand);

        final MetadataElement productMetadata = ecostressProduct.getMetadataRoot().getElementAt(0);
        final double bottomLeftLat = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_SOUTH_BOUNDING_COORDINATE);
        ecostressProduct.setReversed(latitudes[0] == bottomLeftLat);
        if (ecostressProduct.isReversed()) {
            ArrayUtils.swapArray(longitudes);
            ArrayUtils.swapArray(latitudes);
        }

        final double resolutionInKm = RasterUtils.computeResolutionInKm(longitudes, latitudes, width, height);
        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, lonBand.getName(), latBand.getName(), width, height, resolutionInKm);

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
        if (product.getMetadataRoot().getNumElements() < 1) {
            return null;
        }
        final MetadataElement productMetadata = product.getMetadataRoot().getElementAt(0);
        try {
            final double topLeftLon = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_WEST_BOUNDING_COORDINATE);
            final double topRightLon = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_EAST_BOUNDING_COORDINATE);
            final double topLeftLat = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_NORTH_BOUNDING_COORDINATE);
            final double bottomLeftLat = productMetadata.getAttributeDouble(EcostressConstants.ECOSTRESS_STANDARD_METADATA_SOUTH_BOUNDING_COORDINATE);
            return buildCrsGeoCoding(product, topLeftLon, topRightLon, topLeftLat, bottomLeftLat);
        } catch (IllegalArgumentException e) {
            return null;
        }
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
                            final List<Point2D> coordinatePoints = extractPolygon2DCoordinates(remoteProductCoordinates);
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

        try {
            CoordinateReferenceSystem wgs84CRS = DefaultGeographicCRS.WGS84;
            CoordinateReferenceSystem mercatorCRS = CRS.decode("EPSG:3857");
            final Point2D.Double originPointWGS84 = new Point2D.Double(topLeftLon, bottomLeftLat);
            final Point2D.Double endPointWGS84 = new Point2D.Double(topRightLon, topLeftLat);
            final Point2D.Double originPointMercator = GeoUtils.reprojectPoint(originPointWGS84, wgs84CRS, mercatorCRS);
            final Point2D.Double endPointMercator = GeoUtils.reprojectPoint(endPointWGS84, wgs84CRS, mercatorCRS);
            final double pixelSizeX = (endPointMercator.getX() - originPointMercator.getX()) / (productWidth - 1);
            final double pixelSizeY = (endPointMercator.getY() - originPointMercator.getY()) / (productHeight - 1);
            return new CrsGeoCoding(mercatorCRS, productWidth, productHeight, originPointMercator.getX(), endPointMercator.getY(), pixelSizeX, pixelSizeY, 0, 0);
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

    private static boolean isBandRasterReversed(Band targetBand){
        final EcostressProduct ecostressProduct = (EcostressProduct) targetBand.getProduct();
        return ecostressProduct.isReversed();
    }
}