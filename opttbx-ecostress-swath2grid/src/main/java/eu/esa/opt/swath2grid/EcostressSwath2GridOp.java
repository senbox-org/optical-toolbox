package eu.esa.opt.swath2grid;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.dataio.ProductIO;
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
import org.esa.snap.core.datamodel.MetadataAttribute;
import org.esa.snap.core.datamodel.MetadataElement;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.gpf.common.reproject.ReprojectionOp;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.SystemUtils;
import org.esa.snap.engine_utilities.gpf.OperatorUtils;
import org.esa.snap.runtime.Config;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.awt.*;
import java.awt.geom.Point2D;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;

@OperatorMetadata(alias = "EcostressSwath2GridOp",
        category = "Raster/Geometric",
        version = "1.0",
        authors = "Adrian Draghici",
        copyright = "(c) 2024 by CS Group Romania",
        description = "Performs ECOSTRESS Swath to Grid conversion to a specified CRS and radiance to brightness temperature conversion, using the algorithm from 'ECOSTRESS Swath to Grid Conversion Script' by LP DAAC (https://git.earthdata.nasa.gov/projects/LPDUR/repos/ecostress_swath2grid/).")
public class EcostressSwath2GridOp extends Operator {

    public static final String ECOSTRESS_LATITUDE_BAND_NAME = "latitude";
    public static final String ECOSTRESS_LONGITUDE_BAND_NAME = "longitude";
    public static final String ECOSTRESS_GEO_FORMAT = "ECOSTRESS-L1B-GEO";
    public static final String ECOSTRESS_ECO_FORMAT = "ECOSTRESS-.*";
    public static final String ECOSTRESS_SWATH2GRID_OP_AUXDATA_DIR = "EcostressSwath2Grid";
    public static final String ECOSTRESS_BT_PROUCT_BAND_NAME_PATTERN = ".*radiance_(\\d+)";
    private static final String OP_ERROR_SIGNAL = "Swath2GridExecError";

    private static final Logger logger = Logger.getLogger(EcostressSwath2GridOp.class.getName());

    @SourceProduct(alias = "ecoProduct", label = "ECOSTRESS Product to convert", type = ECOSTRESS_ECO_FORMAT, description = "The source product to be converted from Swath to Grid.")
    private Product ecoSourceProduct;
    @SourceProduct(alias = "geoProduct", label = "ECOSTRESS L1B GEO Product", type = ECOSTRESS_GEO_FORMAT, description = "The source GEO product from which the geocoding will be extracted.")
    private Product geoSourceProduct;
    @TargetProduct(description = "The target product which represents the operator's output.")
    private Product targetProduct;
    @Parameter(description = "The projection desired for the output.", label = "Projection", valueSet = {"GEO", "UTM"}, defaultValue = "GEO")
    private String crsIN;
    @Parameter(description = "The UTM zone (EPSG Code) desired for all outputs - only required if needed to override default UTM zone which is assigned based on the center location for each ECOSTRESS granule.", label = "UTM Zone", defaultValue = "auto_lookup")
    private String utmZone;
    @Parameter(description = "The optional argument to convert radiance to brightness temperature for the L1B products", label = "Radiance to brightness", defaultValue = "false")
    private Boolean r2b;

    @Override
    public void initialize() {
        try {
            if (this.ecoSourceProduct != null) {
                if (!this.ecoSourceProduct.getProductType().matches(ECOSTRESS_ECO_FORMAT)) {
                    throw new IllegalArgumentException("The ECOSTRESS Product is not a valid 'ECOSTRESS-L1A/L2A/L1/L3/L4' product.");
                }
                if (this.geoSourceProduct != null) {
                    if (!this.geoSourceProduct.getProductType().matches(ECOSTRESS_GEO_FORMAT)) {
                        throw new IllegalArgumentException("The ECOSTRESS L1B GEO Product is not a valid 'ECOSTRESS-L1B-GEO' product.");
                    }
                    final String geoProductName = this.geoSourceProduct.getName();
                    final String ecoProductName = this.ecoSourceProduct.getName();
                    if (!geoProductMatchesEcoProduct(geoProductName, ecoProductName)) {
                        throw new IllegalArgumentException("The L1B GEO Product doest not match the ECOSTRESS Product.");
                    }
                    targetProduct = new Product("swath2grid_" + ecoSourceProduct.getName(), ecoSourceProduct.getProductType(), ecoSourceProduct.getSceneRasterWidth(), ecoSourceProduct.getSceneRasterHeight());
                }
            }
        } catch (Throwable e) {
            OperatorUtils.catchOperatorException(getId(), e);
        }
    }

    @Override
    public void doExecute(ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Ecostress Swath2Grid", 1);
        try {
            executeOp();
        } catch (Throwable e) {
            this.targetProduct.setName(OP_ERROR_SIGNAL);
            this.targetProduct.setDescription(e.getMessage());
            OperatorUtils.catchOperatorException(getId(), e);
        } finally {
            pm.done();
        }
    }

    private static boolean geoProductMatchesEcoProduct(String geoProductName, String ecoProductName) {
        return geoProductName.contains(ecoProductName.substring(ecoProductName.length() - 37, ecoProductName.length() - 10));
    }

    public void executeOp() throws Exception {
        final Product ecoSourceProduct = new Product(this.ecoSourceProduct.getName(), this.ecoSourceProduct.getProductType(), this.ecoSourceProduct.getSceneRasterWidth(), this.ecoSourceProduct.getSceneRasterHeight());
        ProductUtils.copyProductNodes(ecoSourceProduct, this.ecoSourceProduct);
        for (String ecoSourceProductBandName : this.ecoSourceProduct.getBandNames()) {
            ProductUtils.copyBand(ecoSourceProductBandName, this.ecoSourceProduct, ecoSourceProduct, true);
        }
        final Product georeferencedEcostressProduct;
        final String inputProductName = ecoSourceProduct.getName();
        if (inputProductName.contains("ALEXI_USDA")) {
            applySubsetByBandsDimension(ecoSourceProduct, new Dimension(3000, 3000));
            if (ecoSourceProduct.getNumBands() < 1) {
                logger.warning("No matching SDS layers found for " + inputProductName);
            }
            georeferencedEcostressProduct = ecoSourceProduct.getProduct();
        } else {
            final SwathDefinition swathDef = buildSwathDefinition(geoSourceProduct);
            applySubsetByBandsDimension(ecoSourceProduct, swathDef.getDimension());// Omit NA layers/objs
            if (ecoSourceProduct.getNumBands() < 1) {
                logger.warning("No matching SDS layers found for " + inputProductName);
            }
            if (r2b && inputProductName.contains("RAD")) {
//          ------------------CONVERT RADIANCE TO BRIGHTNESS TEMPERATURE FOR L1B RADIANCE PRODUCTS----------------- #
                convertRadianceToBrightnessTemperature(ecoSourceProduct);
            }
//          ------------------SWATH TO GEOREFERENCED ARRAYS----------------- #
            final AreaDefinition areaDef = buildAreaDefinition(swathDef, crsIN, utmZone);
//          ------------------CONVERT SWATH TO GRID AND APPLY GEO-REFERENCING----------------- #
            georeferencedEcostressProduct = convertSwathToGridAndApplyGeoReferencing(swathDef, areaDef, ecoSourceProduct);
        }
//      ------------------APPLY SCALE FACTOR AND OFFSET----------------- #
        applyScaleFactorAndOffset(georeferencedEcostressProduct);
//      ------------------COPY THE PROCESSED PRODUCT TO THE TARGET OUTPUT PRODUCT OF OPERATOR----------------- #
        ProductUtils.copyProductNodes(georeferencedEcostressProduct, targetProduct);
        for (String georeferencedEcostressProductBandName : georeferencedEcostressProduct.getBandNames()) {
            ProductUtils.copyBand(georeferencedEcostressProductBandName, georeferencedEcostressProduct, targetProduct, true);
        }
        targetProduct.getSceneRasterSize().setSize(georeferencedEcostressProduct.getSceneRasterWidth(), georeferencedEcostressProduct.getSceneRasterHeight());
    }

    private static SwathDefinition buildSwathDefinition(Product geoSourceProduct) throws Exception {
//      Search for relevant SDS inside data file
        Band gLatSD = null;
        Band gLonSD = null;
        for (Band band : geoSourceProduct.getBands()) {
            if (band.getName().contains(ECOSTRESS_LATITUDE_BAND_NAME)) {
                gLatSD = band;
            } else if (band.getName().contains(ECOSTRESS_LONGITUDE_BAND_NAME)) {
                gLonSD = band;
            }
            if (gLatSD != null && gLonSD != null) {
                break;
            }
        }
        if (gLatSD == null) {
            throw new IllegalStateException("no latitude band found");
        }
        if (gLonSD == null) {
            throw new IllegalStateException("no longitude band found");
        }
        gLatSD.readRasterDataFully();
        double[] lat = (double[]) gLatSD.getDataElems();
        final Dimension latDims = gLatSD.getRasterSize();
        gLonSD.readRasterDataFully();
        double[] lon = (double[]) gLonSD.getDataElems();
        final Dimension lonDims = gLonSD.getRasterSize();
        return new SwathDefinition(lon, lat, lonDims, latDims);
    }

    private static AreaDefinition buildAreaDefinition(SwathDefinition swathDef, String crsIN, String utmZone) throws Exception {
        final double[] lat = swathDef.getLats();
        final double[] lon = swathDef.getLons();
        final double midLat = mean(lat);
        final double midLon = mean(lon);
        final double minLat = min(lat);
        final double minLon = min(lon);
        final double maxLat = max(lat);
        final double maxLon = max(lon);
        double[] areaExtent = new double[0];
        double ps = 1;
        CoordinateReferenceSystem targetCrs = CRS.decode("EPSG:4326");
        if (crsIN.equals("UTM")) {
            final String epsg;
            if (utmZone != null && !utmZone.isEmpty() && !utmZone.matches("auto_lookup")) {
                if (!utmZone.matches("^32[6-7]\\d{2}")) {
                    throw new IllegalArgumentException("'UTM Zone' requires the EPSG code (http://spatialreference.org/ref/epsg/) for a UTM zone, and only WGS84 datum is supported");
                }
                epsg = utmZone;
            } else {
                epsg = utmLookup(midLat, midLon);
            }
            targetCrs = CRS.decode("EPSG:" + epsg);
            final Point2D.Double ll = CRSUtil.transform(minLon, minLat, targetCrs);
            final double llLon = ll.x;
            final double llLat = ll.y;
            final Point2D.Double ur = CRSUtil.transform(maxLon, maxLat, targetCrs);
            final double urLon = ur.x;
            final double urLat = ur.y;
            areaExtent = new double[]{llLon, llLat, urLon, urLat};
            ps = 70; // 70 is pixel size (meters)
        } else if (crsIN.equals("GEO")) {
            //Use info from aeqd bbox to calculate output cols/rows/pixel size
            final CoordinateReferenceSystem aeqdCrs = CRS.decode("AUTO2:97003," + midLon + "," + midLat);
            final Point2D.Double ll = CRSUtil.transform(minLon, minLat, aeqdCrs);
            double llLon = ll.x;
            double llLat = ll.y;
            final Point2D.Double ur = CRSUtil.transform(maxLon, maxLat, aeqdCrs);
            double urLon = ur.x;
            double urLat = ur.y;
            areaExtent = new double[]{llLon, llLat, urLon, urLat};
            final int cols = (int) (Math.round(areaExtent[2] - areaExtent[0]) / 70); // 70 m pixel size
            final int rows = (int) (Math.round(areaExtent[3] - areaExtent[1]) / 70);
//              Use no. rows and columns generated above from the aeqd projection to set a representative number of rows and columns, which will then be translated to degrees below, then take the smaller of the two pixel dims to determine output size
            llLon = minLon;
            llLat = minLat;
            urLon = maxLon;
            urLat = maxLat;
            areaExtent = new double[]{llLon, llLat, urLon, urLat};
            final AreaDefinition areaDef = new AreaDefinition(cols, rows, areaExtent, targetCrs);
            ps = Math.min(areaDef.getPixelSizeX(), areaDef.getPixelSizeY());
        }
        final int cols = (int) (Math.round((areaExtent[2] - areaExtent[0]) / ps)); // Calculate the output cols
        final int rows = (int) (Math.round((areaExtent[3] - areaExtent[1]) / ps)); // Calculate the output rows
        return new AreaDefinition(cols, rows, areaExtent, targetCrs);
    }

    private static Product convertSwathToGridAndApplyGeoReferencing(SwathDefinition swathDef, AreaDefinition areaDef, Product ecoSourceProduct) {
        return getReprojectedProduct(ecoSourceProduct, swathDef, areaDef, getFillValue(ecoSourceProduct));
    }

    private static Object getFillValue(Product ecoSourceProduct) {
        Object fillValue = null;
        for (MetadataElement metadataElement : ecoSourceProduct.getMetadataRoot().getElements()) {
            for (MetadataAttribute metadataAttribute : metadataElement.getAttributes()) {
                final Object metadataAttributeValue = metadataAttribute.getDataElems();
                if (metadataAttribute.getName().toLowerCase().contains("fillvalue")) {
                    fillValue = metadataAttributeValue;
                }
            }
        }
        return fillValue;
    }

    private static Product getReprojectedProduct(Product sourceProduct, SwathDefinition sourceGeoDef, AreaDefinition targetGeoDef, Object fillValue) {
        if (sourceProduct.getSceneGeoCoding() == null) {
            attachPixelBasedGeoCoding(sourceProduct, sourceGeoDef);
        }
        final String operatorName = OperatorSpi.getOperatorAlias(ReprojectionOp.class);
        final Map<String, Object> parameterMap = new HashMap<>(9);
        parameterMap.put("crs", targetGeoDef.getCrs().getIdentifiers().iterator().next().toString());
        parameterMap.put("referencePixelX", 0d);
        parameterMap.put("referencePixelY", 0d);
        parameterMap.put("easting", targetGeoDef.getEasting());
        parameterMap.put("northing", targetGeoDef.getNorthing());
        parameterMap.put("pixelSizeX", targetGeoDef.getPixelSizeX());
        parameterMap.put("pixelSizeY", targetGeoDef.getPixelSizeY());
        parameterMap.put("width", targetGeoDef.getWidth());
        parameterMap.put("height", targetGeoDef.getHeight());
        if (fillValue != null) {
            parameterMap.put("noDataValue", fillValue);
        }
        return GPF.createProduct(operatorName, parameterMap, sourceProduct);
    }

    private static void attachPixelBasedGeoCoding(Product sourceProduct, SwathDefinition sourceGeoDef) {
        try {
            final int width = sourceGeoDef.getWidth();
            final int height = sourceGeoDef.getHeight();

            final double[] longitudes = sourceGeoDef.getLons();
            final double[] latitudes = sourceGeoDef.getLats();

            final double resolutionInKm = RasterUtils.computeResolutionInKm(longitudes, latitudes, width, height);
            final Band lonBand = new Band(ECOSTRESS_LONGITUDE_BAND_NAME, ProductData.TYPE_FLOAT64, width, height);
            sourceProduct.addBand(lonBand);
            lonBand.setDataElems(longitudes);
            final Band latBand = new Band(ECOSTRESS_LATITUDE_BAND_NAME, ProductData.TYPE_FLOAT64, width, height);
            sourceProduct.addBand(latBand);
            latBand.setDataElems(latitudes);
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
            sourceProduct.setSceneGeoCoding(geoCoding);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void applyScaleFactorAndOffset(Product targetProduct) throws Exception {
        double scaleFactor = 1;
        double offset = 0;
        for (MetadataElement metadataElement : ecoSourceProduct.getMetadataRoot().getElements()) {
            for (MetadataAttribute metadataAttribute : metadataElement.getAttributes()) {
                final Object metadataAttributeValue = metadataAttribute.getDataElems();
                if (metadataAttribute.getName().toLowerCase().contains("scale")) {
                    scaleFactor = (double) metadataAttributeValue;
                }
                if (metadataAttribute.getName().toLowerCase().contains("offset")) {
                    offset = (double) metadataAttributeValue;
                }
            }
        }
        if (scaleFactor != 1 || offset != 0) {
            for (Band band : targetProduct.getBands()) {
                final ProductData productData = band.getData();
                if (productData.getType() == ProductData.TYPE_FLOAT32) {
                    for (int rowIndex = 0; rowIndex < band.getRasterHeight(); rowIndex++) {
                        final float[] pixels = new float[band.getRasterWidth()];
                        band.readPixels(0, rowIndex, band.getRasterWidth(), 1, pixels);
                        for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
                            pixels[pixelIndex] = (float) (pixels[pixelIndex] * scaleFactor + offset);
                        }
                        band.setPixels(0, rowIndex, band.getRasterWidth(), 1, pixels);
                    }
                } else {
                    for (int rowIndex = 0; rowIndex < band.getRasterHeight(); rowIndex++) {
                        final double[] pixels = new double[band.getRasterWidth()];
                        band.readPixels(0, rowIndex, band.getRasterWidth(), 1, pixels);
                        for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
                            pixels[pixelIndex] = pixels[pixelIndex] * scaleFactor + offset;
                        }
                        band.setPixels(0, rowIndex, band.getRasterWidth(), 1, pixels);
                    }
                }
            }
        }
    }

    private static void applySubsetByBandsDimension(Product ecostressProduct, Dimension bandsDimension) {
        for (Band band : ecostressProduct.getBands()) {
            if (!band.getRasterSize().equals(bandsDimension)) {
                ecostressProduct.removeBand(band);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("parameters:\n-proj (*)\n-geoProduct (*)\n-ecoProduct (*)\n-sds\n-utmZone\n-bt");
        final EcostressSwath2GridOp ecostressSwath2GridOp = getEcostressGeoLocalisationOp(args);
        try {
            ecostressSwath2GridOp.executeOp();
        } catch (Exception e) {
            System.err.println("Execution finished with failure: " + e.getMessage());
        }
        System.out.println("Execution finished successfully.");
    }

    private static EcostressSwath2GridOp getEcostressGeoLocalisationOp(String[] args) {
        try {
            final EcostressSwath2GridOp ecostressSwath2GridOp = new EcostressSwath2GridOp();
            if (args.length < 1) {
                System.err.println("missing parameter: proj (*)");
                throw new IllegalArgumentException("missing parameter: proj (*)");
            }
            ecostressSwath2GridOp.crsIN = args[0];
            if (args.length < 2) {
                System.err.println("missing parameter: geoProduct (*)");
                throw new IllegalArgumentException("missing parameter: geoProduct (*)");
            }
            final String geoProductFile = args[1];
            ecostressSwath2GridOp.geoSourceProduct = ProductIO.readProduct(geoProductFile);
            if (args.length < 3) {
                System.err.println("missing parameter: ecoProduct (*)");
                throw new IllegalArgumentException("missing parameter: ecoProduct (*)");
            }
            final String ecoProductFile = args[2];
            ecostressSwath2GridOp.ecoSourceProduct = ProductIO.readProduct(ecoProductFile);
            final String utmZone;
            if (args.length < 4) {
                utmZone = "";
            } else {
                utmZone = args[3];
            }
            ecostressSwath2GridOp.utmZone = utmZone;
            final String r2b;
            if (args.length < 5) {
                r2b = "";
            } else {
                r2b = args[4];
            }
            ecostressSwath2GridOp.r2b = Boolean.valueOf(r2b);
            return ecostressSwath2GridOp;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static double mean(double[] arr) {
        double sum = 0;
        for (double arrItem : arr) {
            sum += arrItem;
        }
        return sum / arr.length;
    }

    private static double min(double[] arr) {
        double min = arr[0];
        for (double arrItem : arr) {
            if (arrItem < min) {
                min = arrItem;
            }
        }
        return min;
    }

    private static double max(double[] arr) {
        double max = arr[0];
        for (double arrItem : arr) {
            if (arrItem > max) {
                max = arrItem;
            }
        }
        return max;
    }

    private static String utmLookup(double lat, double lon) {
        int utmValue = (int) (((Math.floor(lon + 180) / 6) % 60) + 1);
        final String utm;
        if (utmValue / 10 == 0) {
            utm = "0" + utmValue;
        } else {
            utm = "" + utmValue;
        }
        final String epsgCode;
        if (lat >= 0) {
            epsgCode = "326" + utm;
        } else {
            epsgCode = "327" + utm;
        }
        return epsgCode;
    }

    private static void convertRadianceToBrightnessTemperature(Product targetProduct) throws Exception {
        try (Product ecostressBrightnessTemperatureProduct = loadAndGetEcostressBrightnessTemperatureProduct()) {
            for (String targetBandName : targetProduct.getBandNames()) {
                final Band targetBand = targetProduct.getBand(targetBandName);
                if (targetBandName.matches(ECOSTRESS_BT_PROUCT_BAND_NAME_PATTERN)) {
                    targetBand.readRasterDataFully();
                    final Band targetBandB2T = new Band(targetBand.getName(), targetBand.getDataType(), targetBand.getRasterWidth(), targetBand.getRasterHeight());
                    ProductUtils.copyRasterDataNodeProperties(targetBand, targetBandB2T);
                    final int ecostressBrightnessTemperatureProductBandIndex = Integer.parseInt(targetBandName.replaceAll(ECOSTRESS_BT_PROUCT_BAND_NAME_PATTERN, "$1")) - 1;
                    final Band ecostressBrightnessTemperatureProductBand = ecostressBrightnessTemperatureProduct.getBandAt(ecostressBrightnessTemperatureProductBandIndex);
                    ecostressBrightnessTemperatureProductBand.readRasterDataFully();
                    final double[] lut = (double[]) ecostressBrightnessTemperatureProductBand.getDataElems();
                    final float[] targetBandData = (float[]) targetBand.getDataElems();
                    final float[] bt = new float[targetBandData.length];
                    for (int i = 0; i < targetBandData.length; i++) {
                        final double ecoSDflat = targetBandData[i] >= 0 && targetBandData[i] <= 60 ? targetBandData[i] : 0;
                        if (ecoSDflat != 0) {
                            final int indexLUT = (int) (ecoSDflat / 0.001);
//                          Interpolate the LUT values for each radiance based on two nearest LUT values
                            final double radianceX0 = indexLUT * 0.001;
                            final double radianceX1 = radianceX0 + 0.001;
                            final double factor0 = (radianceX1 - ecoSDflat) / 0.001;
                            final double factor1 = (ecoSDflat - radianceX0) / 0.001;
                            bt[i] = (float) ((factor0 * lut[indexLUT]) + (factor1 * lut[indexLUT + 1]));
                        } else {
                            bt[i] = targetBandData[i];
                        }
                    }
                    targetBandB2T.setDataElems(bt);
                    targetProduct.removeBand(targetBand);
                    targetProduct.addBand(targetBandB2T);
                }
            }
        }
    }

    private static Product loadAndGetEcostressBrightnessTemperatureProduct() throws Exception {
        final Path ecostressSwath2gridOpAuxdataDir = SystemUtils.getAuxDataPath().resolve(ECOSTRESS_SWATH2GRID_OP_AUXDATA_DIR);
        Files.createDirectories(ecostressSwath2gridOpAuxdataDir);
        final URL ecostressBrightnessTemperatureProductFileURI = EcostressSwath2GridOp.class.getResource("EcostressBrightnessTemperatureV01.h5");
        if (ecostressBrightnessTemperatureProductFileURI == null) {
            throw new IllegalArgumentException("Ecostress Brightness Temperature Product not found, download the table at https://git.earthdata.nasa.gov/projects/LPDUR/repos/ecostress_swath2grid/browse");
        }
        final Path ecostressBrightnessTemperatureProductResourcePath = Path.of(ecostressBrightnessTemperatureProductFileURI.toURI());
        final Path ecostressBrightnessTemperatureProductPath = ecostressSwath2gridOpAuxdataDir.resolve(ecostressBrightnessTemperatureProductResourcePath.getFileName());
        if (!Files.exists(ecostressBrightnessTemperatureProductPath)) {
            Files.copy(ecostressBrightnessTemperatureProductResourcePath, ecostressBrightnessTemperatureProductPath);
        }
        return ProductIO.readProduct(ecostressBrightnessTemperatureProductPath.toFile());
    }

    private static class AreaDefinition {
        private final int width;
        private final int height;
        private final CoordinateReferenceSystem crs;
        private final double pixelSizeX;
        private final double pixelSizeY;
        private final Point2D.Double pixelUpperLeft;


        public AreaDefinition(int width, int height, double[] areaExtent, CoordinateReferenceSystem crs) {
            this.width = width;
            this.height = height;
            this.crs = crs;
            final double minLon = areaExtent[0];
            final double minLat = areaExtent[1];
            final double maxLon = areaExtent[2];
            final double maxLat = areaExtent[3];
            this.pixelSizeX = (maxLon - minLon) / width;
            this.pixelSizeY = (maxLat - minLat) / height;
            this.pixelUpperLeft = new Point2D.Double(minLon + pixelSizeX / 2, maxLat - pixelSizeY / 2);
        }

        public double getPixelSizeX() {
            return pixelSizeX;
        }

        public double getPixelSizeY() {
            return pixelSizeY;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public double getEasting() {
            return pixelUpperLeft.getX();
        }

        public double getNorthing() {
            return pixelUpperLeft.getY();
        }

        public CoordinateReferenceSystem getCrs() {
            return crs;
        }
    }

    private static class SwathDefinition {
        private final double[] lons;
        private final double[] lats;

        private final Dimension dimension;

        public SwathDefinition(double[] lons, double[] lats, Dimension lonDims, Dimension latDims) {
            this.lons = lons;
            this.lats = lats;
            if (!lonDims.equals(latDims)) {
                throw new IllegalArgumentException("lon and lat arrays must have the same dimensions.");
            }
            this.dimension = lonDims;
        }

        public double[] getLons() {
            return lons;
        }

        public double[] getLats() {
            return lats;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public int getWidth() {
            return dimension.width;
        }

        public int getHeight() {
            return dimension.height;
        }
    }

    private static class CRSUtil {

        public static Point2D.Double transform(double lon, double lat, CoordinateReferenceSystem targetCRS) throws Exception {
            return transform(lon, lat, CRS.decode("EPSG:4326"), targetCRS);
        }

        public static Point2D.Double transform(double lon, double lat, CoordinateReferenceSystem sourceCRS, CoordinateReferenceSystem targetCRS) throws Exception {
            final MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            final double[] sourceCoords = new double[]{lon, lat};
            final double[] targetCoords = new double[2];
            transform.transform(sourceCoords, 0, targetCoords, 0, 1);
            return new Point2D.Double(targetCoords[0], targetCoords[1]);
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(EcostressSwath2GridOp.class);
        }
    }
}
