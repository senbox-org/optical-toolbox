package eu.esa.opt.dataio.enmap;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.core.VirtualDir;
import eu.esa.opt.dataio.TarUtils;
import eu.esa.opt.dataio.enmap.imgReader.EnmapImageReader;
import eu.esa.snap.core.dataio.cache.CacheDataProvider;
import eu.esa.snap.core.dataio.cache.CacheManager;
import eu.esa.snap.core.dataio.cache.DataBuffer;
import eu.esa.snap.core.dataio.cache.ProductCache;
import eu.esa.snap.core.dataio.cache.VariableDescriptor;
import eu.esa.snap.core.datamodel.band.BandUsingReaderDirectly;
import org.esa.snap.core.dataio.AbstractProductReader;
import org.esa.snap.core.dataio.ProductIO;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.geocoding.ComponentFactory;
import org.esa.snap.core.dataio.geocoding.ComponentGeoCoding;
import org.esa.snap.core.dataio.geocoding.GeoChecks;
import org.esa.snap.core.dataio.geocoding.GeoRaster;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.engine_utilities.dataio.VirtualDirTgz;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static eu.esa.opt.dataio.enmap.EnmapFileUtils.*;


class EnmapProductReader extends AbstractProductReader implements CacheDataProvider {


    private static final int KM_IN_METERS = 1000;
    static final String SPECTRAL_CACHE_VARIABLE_NAME = "ENMAP_SPECTRAL_CUBE";
    private static final String SCENE_AZIMUTH_TPG_NAME = "scene_azimuth";
    private static final String SUN_AZIMUTH_TPG_NAME = "sun_azimuth";
    private static final String SUN_ELEVATION_TPG_NAME = "sun_elevation";
    private static final String ACROSS_OFF_NADIR_TPG_NAME = "across_off_nadir";
    private static final String ALONG_OFF_NADIR_TPG_NAME = "along_off_nadir";

    private static final String CANNOT_READ_PRODUCT_MSG = "Cannot read product";

    private final Map<String, Integer> spectralBandLayerIndexMap = new HashMap<>();
    private final Map<String, CachedBandBinding> cachedBandBindings = new HashMap<>();
    private final Set<String> registeredCacheVariables = new HashSet<>();
    private final List<EnmapImageReader> imageReaderList = new ArrayList<>();
    private ProductCache productCache;
    private EnmapMultiCubeCacheProvider cacheProvider;
    private VirtualDir dataDir;
    private VirtualDir tgzDataDir;

    private boolean isNonCompliantProduct = false;


    EnmapProductReader(EnmapProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        dataDir = null;
        tgzDataDir = null;
        cacheProvider = new EnmapMultiCubeCacheProvider();
        productCache = new ProductCache(this);
        CacheManager.getInstance().register(productCache);
    }


    private static TiePointGrid addTPG(Product product, String tpgName, double[] tpgValue) {
        int gridWidth = 2;
        int gridHeight = 2;
        int gridSamplingX = product.getSceneRasterWidth();
        int gridSamplingY = product.getSceneRasterHeight();
        float[] tpData = new float[tpgValue.length];
        IntStream.range(0, tpgValue.length).forEach(i -> tpData[i] = (float) tpgValue[i]);
        TiePointGrid tpg = new TiePointGrid(tpgName, gridWidth, gridHeight, 0, 0,
                gridSamplingX, gridSamplingY, tpData, true);
        tpg.setUnit("DEG");
        product.addTiePointGrid(tpg);
        return tpg;
    }

    private static void addTiePointGeoCoding(Product product, EnmapMetadata meta) throws IOException {
        String lonName = "longitude";
        String latName = "latitude";
        double[] cornerLatitudes = meta.getCornerLatitudes();
        double[] cornerLongitudes = meta.getCornerLongitudes();
        addTPG(product, latName, cornerLatitudes);
        TiePointGrid lonGrid = addTPG(product, lonName, cornerLongitudes);
        double pixelSizeKm = meta.getPixelSize() / KM_IN_METERS;
        GeoRaster geoRaster = new GeoRaster(cornerLongitudes, cornerLatitudes,
                lonName, latName, lonGrid.getGridWidth(), lonGrid.getGridHeight(),
                product.getSceneRasterWidth(), product.getSceneRasterHeight(), pixelSizeKm,
                lonGrid.getOffsetX(), lonGrid.getOffsetY(),
                lonGrid.getSubSamplingX(), lonGrid.getSubSamplingY());
        ComponentGeoCoding sceneGeoCoding = new ComponentGeoCoding(geoRaster,
                ComponentFactory.getForward(TiePointBilinearForward.KEY),
                ComponentFactory.getInverse(TiePointInverse.KEY), GeoChecks.ANTIMERIDIAN, DefaultGeographicCRS.WGS84);
        sceneGeoCoding.initialize();
        product.setSceneGeoCoding(sceneGeoCoding);
    }

    static String getEPSGCode(String projection) throws Exception {
        int code;
        if (projection.startsWith("UTM")) {
            final int utmZone = Integer.parseInt(projection.substring(8, projection.lastIndexOf('_')));
            if (projection.endsWith("North")) {
                code = 32600 + utmZone;
            } else {
                code = 32700 + utmZone;
            }
        } else if ("LAEA-ETRS89".equals(projection)) {
            code = 3035;
        } else if ("Geographic".equals(projection)) {
            code = 4326;
        } else {
            throw new IllegalArgumentException(String.format("Cannot decode EPSG code from projection string '%s'", projection));
        }
        return "EPSG:" + code;
    }

    @Override
    protected Product readProductNodesImpl() throws IOException {
        Path path = InputTypes.toPath(getInput());
        if (TarUtils.isTar(path)) {
            tgzDataDir = new VirtualDirTgz(path);
            final String[] fileNames = tgzDataDir.listAllFiles();

            String zipFileName = null;
            for (final String fileName : fileNames) {
                if (fileName.endsWith(".ZIP") || fileName.endsWith(".zip")) {
                    zipFileName = fileName;
                    break;
                }
            }

            final File tgzDataDirFile = tgzDataDir.getFile(zipFileName);
            path = tgzDataDirFile.toPath();
        } else if (!isZip(path)) {
            path = path.getParent();
        }

        dataDir = VirtualDir.create(path.toFile());
        if (dataDir == null) {
            throw new IOException(String.format("%s%nVirtual directory could not be created", CANNOT_READ_PRODUCT_MSG));
        }

        String[] fileNames = dataDir.listAllFiles();
        String metadataFile = getMetadataFile(fileNames);
        EnmapMetadata meta = EnmapMetadata.create(dataDir.getInputStream(metadataFile));

        this.isNonCompliantProduct = checkIfProductIsNonCompliant(dataDir);
        if (this.isNonCompliantProduct) {
            meta.setNonCompliantProduct(true);
        }

        String productFormat = meta.getProductFormat();
        if (!ProductFormat.GeoTIFF_Metadata.name().equals(ProductFormat.toEnumName(productFormat))) {
            throw new IllegalStateException(String.format("The product format '%s' is not supported.%n" +
                    "Currently only '%s' is supported", productFormat, ProductFormat.GeoTIFF_Metadata.asEnmapFormatName()));
        }

        Dimension dimension = meta.getSceneDimension();
        Product product = new Product(meta.getProductName(), meta.getProductType(), dimension.width, dimension.height);
        product.setStartTime(meta.getStartTime());
        product.setEndTime(meta.getStopTime());

        addGeoCoding(product, meta);
        addSpectralBands(product, meta);
        addTiePointGrids(product, meta);
        addQualityLayers(product, meta);
        addMetadata(product, meta);

        product.setAutoGrouping("band:PIXELMASK:QUALITY");

        return product;
    }

    private static void addMetadata(Product product, EnmapMetadata meta) throws IOException {
        meta.insertInto(product.getMetadataRoot());
    }

    private void addQualityLayers(Product product, EnmapMetadata meta) throws IOException {
        addClassesQl(product, meta);
        addHazeQl(product, meta);
        addCloudQl(product, meta);
        addCirrusQl(product, meta);
        addCloudShadowQl(product, meta);
        addSnowQl(product, meta);
        addTestFlagsQl(product, meta);
        addPixelMasksQl(product, dataDir, meta);
    }

    private void addClassesQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_CLASSES_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_CLASSES_LAND.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CLASSES_LAND.addMaskTo(product);
        QualityLayerInfo.QL_CLASSES_WATER.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CLASSES_WATER.addMaskTo(product);
        QualityLayerInfo.QL_CLASSES_BG.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CLASSES_BG.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addCloudQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_CLOUD_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_CLOUD_CLOUD.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CLOUD_CLOUD.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addCloudShadowQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_CLOUDSHADOW_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_CLOUDSHADOW_SHADOW.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CLOUDSHADOW_SHADOW.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addHazeQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_HAZE_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_HAZE_HAZE.addFlagTo(flagCoding);
        QualityLayerInfo.QL_HAZE_HAZE.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addCirrusQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_CIRRUS_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_CIRRUS_THIN.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CIRRUS_THIN.addMaskTo(product);
        QualityLayerInfo.QL_CIRRUS_MEDIUM.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CIRRUS_MEDIUM.addMaskTo(product);
        QualityLayerInfo.QL_CIRRUS_THICK.addFlagTo(flagCoding);
        QualityLayerInfo.QL_CIRRUS_THICK.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addSnowQl(Product product, EnmapMetadata meta) throws IOException {
        String qualityKey = QUALITY_SNOW_KEY;
        FlagCoding flagCoding = new FlagCoding(qualityKey);
        product.getFlagCodingGroup().add(flagCoding);
        QualityLayerInfo.QL_SNOW_SNOW.addFlagTo(flagCoding);
        QualityLayerInfo.QL_SNOW_SNOW.addMaskTo(product);

        EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
        imageReaderList.add(qualityReader);

        addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
    }

    private void addPixelMasksQl(Product product, VirtualDir dataDir, EnmapMetadata meta) throws IOException {
        EnmapImageReader pixelMaskReader = EnmapImageReader.createPixelMaskReader(dataDir, meta);
        imageReaderList.add(pixelMaskReader);

        FlagCoding flagCoding = new FlagCoding(QUALITY_PIXELMASK_KEY);
        flagCoding.addFlag("Defective", 1, "Defective pixel");
        product.getFlagCodingGroup().add(flagCoding);

        ensureCubeRegistered(cubeVar(QUALITY_PIXELMASK_KEY), pixelMaskReader, product.getSceneRasterWidth(), product.getSceneRasterHeight(), pixelMaskReader.getNumImages(), ProductData.TYPE_UINT8);

        int[] spectralIndices = meta.getSpectralIndices();
        for (int i = 0; i < meta.getNumSpectralBands(); i++) {
            String flagBandName = String.format("%s_%03d", QUALITY_PIXELMASK_KEY, spectralIndices[i]);
            Band flagBand = new BandUsingReaderDirectly(flagBandName, ProductData.TYPE_UINT8, product.getSceneRasterWidth(), product.getSceneRasterHeight());

            flagBand.setSampleCoding(flagCoding);
            flagBand.setNoDataValueUsed(true);
            flagBand.setNoDataValue(meta.getPixelmaskBackgroundValue());

            product.addBand(flagBand);
            cachedBandBindings.put(flagBandName, new CachedBandBinding(cubeVar(QUALITY_PIXELMASK_KEY), i));
        }
        QualityLayerInfo.QL_PM_DEFECTIVE_SERIES.addMasksTo(product, meta);

    }

    private void addTestFlagsQl(Product product, EnmapMetadata meta) throws IOException {
        if (EnmapMetadata.PROCESSING_LEVEL.L1B == meta.getProcessingLevel()) {
            String vnirQualityKey = QUALITY_TESTFLAGS_VNIR_KEY;
            FlagCoding vnirFlagCoding = new FlagCoding(vnirQualityKey);
            product.getFlagCodingGroup().add(vnirFlagCoding);

            QualityLayerInfo.QL_TF_VNIR_NOMINAL.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_NOMINAL.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_REDUCED.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_REDUCED.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_LOW.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_LOW.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_NOT.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_NOT.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_INTERPOLATED_SWIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_INTERPOLATED_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_INTERPOLATED_VNIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_INTERPOLATED_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_SATURATION_SWIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_SATURATION_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_SATURATION_VNIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_SATURATION_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_ARTEFACT_SWIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_ARTEFACT_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_VNIR_ARTEFACT_VNIR.addFlagTo(vnirFlagCoding);
            QualityLayerInfo.QL_TF_VNIR_ARTEFACT_VNIR.addMaskTo(product);

            EnmapImageReader qualityVnirReader = EnmapImageReader.createImageReader(dataDir, meta, vnirQualityKey);
            imageReaderList.add(qualityVnirReader);

            addCachedFlagBand(product, vnirQualityKey, vnirFlagCoding, qualityVnirReader, 0, cubeVar(vnirQualityKey), ProductData.TYPE_UINT8, 1);

            String swirQualityKey = QUALITY_TESTFLAGS_SWIR_KEY;
            FlagCoding swirFlagCoding = new FlagCoding(swirQualityKey);
            product.getFlagCodingGroup().add(swirFlagCoding);

            QualityLayerInfo.QL_TF_SWIR_NOMINAL.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_NOMINAL.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_REDUCED.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_REDUCED.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_LOW.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_LOW.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_NOT.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_NOT.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_INTERPOLATED_SWIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_INTERPOLATED_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_INTERPOLATED_VNIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_INTERPOLATED_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_SATURATION_SWIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_SATURATION_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_SATURATION_VNIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_SATURATION_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_ARTEFACT_SWIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_ARTEFACT_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SWIR_ARTEFACT_VNIR.addFlagTo(swirFlagCoding);
            QualityLayerInfo.QL_TF_SWIR_ARTEFACT_VNIR.addMaskTo(product);

            EnmapImageReader qualitySwirReader = EnmapImageReader.createImageReader(dataDir, meta, swirQualityKey);
            imageReaderList.add(qualitySwirReader);

            addCachedFlagBand(product, swirQualityKey, swirFlagCoding, qualitySwirReader, 0, cubeVar(swirQualityKey), ProductData.TYPE_UINT8, 1);
        } else {
            String qualityKey = QUALITY_TESTFLAGS_KEY;
            FlagCoding flagCoding = new FlagCoding(qualityKey);
            product.getFlagCodingGroup().add(flagCoding);
            QualityLayerInfo.QL_TF_NOMINAL.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_NOMINAL.addMaskTo(product);
            QualityLayerInfo.QL_TF_REDUCED.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_REDUCED.addMaskTo(product);
            QualityLayerInfo.QL_TF_LOW.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_LOW.addMaskTo(product);
            QualityLayerInfo.QL_TF_NOT.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_NOT.addMaskTo(product);
            QualityLayerInfo.QL_TF_INTERPOLATED_SWIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_INTERPOLATED_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_INTERPOLATED_VNIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_INTERPOLATED_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SATURATION_SWIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_SATURATION_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_SATURATION_VNIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_SATURATION_VNIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_ARTEFACT_SWIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_ARTEFACT_SWIR.addMaskTo(product);
            QualityLayerInfo.QL_TF_ARTEFACT_VNIR.addFlagTo(flagCoding);
            QualityLayerInfo.QL_TF_ARTEFACT_VNIR.addMaskTo(product);

            EnmapImageReader qualityReader = EnmapImageReader.createImageReader(dataDir, meta, qualityKey);
            imageReaderList.add(qualityReader);

            addCachedFlagBand(product, qualityKey, flagCoding, qualityReader, 0, cubeVar(qualityKey), ProductData.TYPE_UINT8, 1);
        }
    }


    private static void addTiePointGrids(Product product, EnmapMetadata meta) throws IOException {
        addTPG(product, SCENE_AZIMUTH_TPG_NAME, meta.getSceneAzimuthAngles());
        addTPG(product, SUN_AZIMUTH_TPG_NAME, meta.getSunAzimuthAngles());
        addTPG(product, SUN_ELEVATION_TPG_NAME, meta.getSunElevationAngles());
        addTPG(product, ACROSS_OFF_NADIR_TPG_NAME, meta.getAcrossOffNadirAngles());
        addTPG(product, ALONG_OFF_NADIR_TPG_NAME, meta.getAlongOffNadirAngles());
    }


    private void addSpectralBands(Product product, EnmapMetadata meta) throws IOException {

        EnmapImageReader spectralImageReader = EnmapImageReader.createSpectralReader(dataDir, meta);
        imageReaderList.add(spectralImageReader);

        product.setPreferredTileSize(spectralImageReader.getTileDimension());

        int[] spectralIndices = meta.getSpectralIndices();
        final int numSpectralLayers = spectralImageReader.getNumImages();
        final int sceneWidth = product.getSceneRasterWidth();
        final int sceneHeight = product.getSceneRasterHeight();
        int dataType = meta.getSpectralDataType();

        ensureCubeRegistered(SPECTRAL_CACHE_VARIABLE_NAME, spectralImageReader, product.getSceneRasterWidth(), product.getSceneRasterHeight(), spectralImageReader.getNumImages(), dataType);

        for (int i = 0; i < numSpectralLayers; i++) {
            int spectralIndex = spectralIndices[i];
            String bandName = String.format("band_%03d", spectralIndex);
            Band band = new BandUsingReaderDirectly(bandName, dataType, sceneWidth, sceneHeight);
            band.setSpectralBandIndex(spectralIndex - 1);
            band.setSpectralWavelength(meta.getCentralWavelength(i));
            band.setSpectralBandwidth(meta.getBandwidth(i));
            band.setDescription(meta.getSpectralBandDescription(i));
            band.setUnit(meta.getSpectralUnit());
            band.setScalingFactor(meta.getBandScaling(i));
            band.setScalingOffset(meta.getBandOffset(i));
            band.setNoDataValue(meta.getSpectralBackgroundValue());
            band.setNoDataValueUsed(true);
            spectralBandLayerIndexMap.put(bandName, i);
            product.addBand(band);
        }

    }

    private void addGeoCoding(Product product, EnmapMetadata meta) throws IOException {
        switch (meta.getProcessingLevel()) {
            case L1B:
                addTiePointGeoCoding(product, meta);
                break;
            case L1C:
            case L2A:
                addCrsGeoCoding(product, meta);
                break;
        }
    }

    @Override
    protected void readBandRasterDataImpl(int sourceOffsetX, int sourceOffsetY, int sourceWidth, int sourceHeight,
                                          int sourceStepX, int sourceStepY,
                                          Band destBand, int destOffsetX, int destOffsetY, int destWidth, int destHeight,
                                          ProductData destBuffer, ProgressMonitor pm) throws IOException {
        final Integer spectralLayer = spectralBandLayerIndexMap.get(destBand.getName());
        if (spectralLayer != null) {
            readFromCache(SPECTRAL_CACHE_VARIABLE_NAME, spectralLayer, destOffsetX, destOffsetY, destWidth, destHeight, destBuffer);
            return;
        }

        CachedBandBinding cb = cachedBandBindings.get(destBand.getName());
        if (cb != null) {
            readFromCache(cb.variableName, cb.layerIndex, destOffsetX, destOffsetY, destWidth, destHeight, destBuffer);
            return;
        }

        throw new IOException("No cache binding for band " + destBand.getName());
    }

    @Override
    public VariableDescriptor getVariableDescriptor(String variableName) throws IOException {
        if (cacheProvider == null) {
            throw new IOException("Cache provider not initialized");
        }
        return cacheProvider.getVariableDescriptor(variableName);
    }

    @Override
    public DataBuffer readCacheBlock(String variableName, int[] offsets, int[] shapes, ProductData targetData) throws IOException {
        if (cacheProvider == null) {
            throw new IOException("Cache provider not initialized");
        }
        return cacheProvider.readCacheBlock(variableName, offsets, shapes, targetData);
    }

    @Override
    public void close() {
        if (productCache != null) {
            CacheManager.getInstance().remove(productCache);
            productCache = null;
        }
        if (cacheProvider != null) {
            cacheProvider.clear();
            cacheProvider = null;
        }
        registeredCacheVariables.clear();
        cachedBandBindings.clear();
        spectralBandLayerIndexMap.clear();

        for (EnmapImageReader reader : imageReaderList) {
            reader.close();
        }
        imageReaderList.clear();

        if (dataDir != null) {
            dataDir.close();
            dataDir = null;
        }
        if (tgzDataDir != null) {
            tgzDataDir.close();
            tgzDataDir = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    private void addCrsGeoCoding(Product product, EnmapMetadata meta) throws IOException {
        GeoReferencing geoReferencing = meta.getGeoReferencing();
        try {
            String epsgCode = getEPSGCode(geoReferencing.projection);
            CoordinateReferenceSystem coordinateReferenceSystem = CRS.decode(epsgCode);
            Dimension dimension = meta.getSceneDimension();
            double resolution = geoReferencing.resolution;
            // todo - easting and northing should be provided in metadata but are not in the test data
            // todo - we need to read it from one of the geotiff files.
//                double easting = geoReferencing.easting;
//                double northing = geoReferencing.northing;
            Point2D eastingNorthing = getEastingNorthing(meta);
            if (eastingNorthing != null) {
                CrsGeoCoding crsGeoCoding = new CrsGeoCoding(coordinateReferenceSystem,
                        (int) dimension.getWidth(), (int) dimension.getHeight(),
                        eastingNorthing.getX(), eastingNorthing.getY(),
                        resolution, resolution, geoReferencing.refX, geoReferencing.refY);
                product.setSceneGeoCoding(crsGeoCoding);
            }
        } catch (Exception e) {
            throw new IOException(CANNOT_READ_PRODUCT_MSG, e);
        }
    }

    private Point2D getEastingNorthing(EnmapMetadata meta) throws IOException {
        Map<String, String> fileNameMap = meta.getFileNameMap();
        String dataFileName = fileNameMap.get(QUALITY_CLASSES_KEY);
        InputStream inputStream = getInputStream(dataDir, dataFileName, this.isNonCompliantProduct);
        ProductReader reader = null;
        try {
            reader = ProductIO.getProductReader("GeoTIFF");
            Product product = reader.readProductNodes(inputStream, null);
            GeoCoding geoCoding = product.getSceneGeoCoding();
            MathTransform i2m = geoCoding.getImageToMapTransform();
            if (i2m instanceof AffineTransform) {
                AffineTransform i2mAT = (AffineTransform) i2m;
                return new Point2D.Double(i2mAT.getTranslateX(), i2mAT.getTranslateY());
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    private static String getMetadataFile(String[] fileNames) throws IOException {
        Optional<String> first = Arrays.stream(fileNames).filter(s -> s.endsWith(METADATA_SUFFIX)).findFirst();
        return first.orElseThrow(() -> new IOException("Metadata file not found"));
    }

    private void readFromCache(String variableName, int layerIndex, int x, int y, int w, int h, ProductData destBuffer) throws IOException {
        VariableDescriptor desc = cacheProvider.getVariableDescriptor(variableName);
        int[] offsets = (desc.layers <= 1) ? new int[]{y, x} : new int[]{layerIndex, y, x};
        int[] shapes  = (desc.layers <= 1) ? new int[]{h, w} : new int[]{1, h, w};

        DataBuffer target = new DataBuffer(destBuffer, new int[]{y, x}, new int[]{h, w});
        productCache.read(variableName, offsets, shapes, target);
    }

    private void addCachedFlagBand(Product product, String bandName, FlagCoding flagCoding, EnmapImageReader reader, int layerIndex, String variableName, int dataType, int tileLayers) throws IOException {
        if (layerIndex < 0 || layerIndex >= reader.getNumImages()) {
            throw new IOException(String.format("Layer index %d out of range for band '%s'.", layerIndex, bandName));
        }

        ensureCubeRegistered(variableName, reader, product.getSceneRasterWidth(), product.getSceneRasterHeight(), reader.getNumImages(), dataType, tileLayers);

        Band b = new BandUsingReaderDirectly(bandName, dataType, product.getSceneRasterWidth(), product.getSceneRasterHeight());
        b.setSampleCoding(flagCoding);
        product.addBand(b);

        cachedBandBindings.put(bandName, new CachedBandBinding(variableName, layerIndex));
    }

    private static String cubeVar(String key) {
        return "ENMAP_" + key + "_CUBE";
    }

    private void ensureCubeRegistered(String variableName, EnmapImageReader reader, int sceneWidth, int sceneHeight, int numLayers, int dataType, int tileLayers) throws IOException {
        if (registeredCacheVariables.add(variableName)) {
            cacheProvider.addCube(variableName, reader, sceneWidth, sceneHeight, numLayers, dataType, tileLayers);
        }
    }

    private void ensureCubeRegistered(String variableName, EnmapImageReader reader, int sceneWidth, int sceneHeight, int numLayers, int dataType) throws IOException {
        if (registeredCacheVariables.add(variableName)) {
            cacheProvider.addCube(variableName, reader, sceneWidth, sceneHeight, numLayers, dataType);
        }
    }

    private static final class CachedBandBinding {
        final String variableName;
        final int layerIndex;

        CachedBandBinding(String variableName, int layerIndex) {
            this.variableName = variableName;
            this.layerIndex = layerIndex;
        }
    }
}
