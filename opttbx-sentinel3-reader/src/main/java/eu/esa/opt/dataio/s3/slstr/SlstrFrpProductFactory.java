package eu.esa.opt.dataio.s3.slstr;

import com.bc.ceres.glevel.support.DefaultMultiLevelImage;
import com.bc.ceres.glevel.support.DefaultMultiLevelModel;
import com.bc.ceres.glevel.support.DefaultMultiLevelSource;
import eu.esa.opt.dataio.s3.manifest.Manifest;
import eu.esa.opt.dataio.s3.Sentinel3ProductReader;
import eu.esa.snap.core.datamodel.band.SparseDataBand;
import org.esa.snap.core.dataio.geocoding.*;
import org.esa.snap.core.dataio.geocoding.forward.TiePointBilinearForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.dataio.geocoding.inverse.TiePointInverse;
import org.esa.snap.core.dataio.geocoding.util.RasterUtils;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.runtime.Config;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class SlstrFrpProductFactory extends SlstrProductFactory {

    private static final double RESOLUTION_IN_KM = 1.0;
    private final static String SYSPROP_SLSTR_FRP_PIXEL_CODING_INVERSE = "opttbx.reader.slstr.frp.pixelGeoCoding.inverse";

    private final Map<String, GeoCoding> geoCodingMap;
    private final Map<String, Double> gridIndexToTrackOffset;
    private final Map<String, Double> gridIndexToStartOffset;

    public SlstrFrpProductFactory(Sentinel3ProductReader productReader) {
        super(productReader);
        geoCodingMap = new HashMap<>();
        gridIndexToTrackOffset = new HashMap<>();
        gridIndexToStartOffset = new HashMap<>();
    }

    protected void addProductSpecificMetadata(Product targetProduct) {
        final List<Product> openProductList = getOpenProductList();
        for (final Product sourceProduct : openProductList) {
            final String sourceProductName = sourceProduct.getName();

            updateOffsetsAndResolutions(sourceProduct, sourceProductName);

            final MetadataElement[] variableAttributes = sourceProduct.getMetadataRoot().getElement("Variable_Attributes").getElements();
            if (variableAttributes.length == 0) {
                continue;
            }

            final MetadataElement metadataRoot = targetProduct.getMetadataRoot();
            final MetadataElement frpElement = new MetadataElement(sourceProductName);
            for (final MetadataElement element : variableAttributes) {
                if (!frpElement.containsElement(element.getDisplayName())) {
                    frpElement.addElement(element.createDeepClone());
                }
            }
            metadataRoot.addElement(frpElement);
        }
    }

    private void updateOffsetsAndResolutions(Product sourceProduct, String sourceProductName) {
        final String identifier = getGridIndex(sourceProductName);
        final MetadataElement globalAttributes = sourceProduct.getMetadataRoot().getElement("Global_Attributes");
        if (!gridIndexToStartOffset.containsKey(identifier)) {
            gridIndexToStartOffset.put(identifier, globalAttributes.getAttributeDouble("start_offset"));
        }
        if (!gridIndexToTrackOffset.containsKey(identifier)) {
            gridIndexToTrackOffset.put(identifier, globalAttributes.getAttributeDouble("track_offset"));
        }
        if ("in".equals(identifier)) {
            setReferenceStartOffset(getStartOffset(identifier));
            setReferenceTrackOffset(getTrackOffset(identifier));
            setReferenceResolutions(getResolutions(identifier));
        }
    }

    @Override
    protected Product findMasterProduct() {
        List<Product> openProductList = getOpenProductList();
        for (final Product p : openProductList) {
            if (p.getName().contains("FRP_in")) {
                return p;
            }
        }
        return null;
    }

    @Override
    protected Double getStartOffset(String gridIndex) {
        if (gridIndexToStartOffset.containsKey(gridIndex)) {
            return gridIndexToStartOffset.get(gridIndex);
        }
        return 0.0;
    }

    @Override
    protected Double getTrackOffset(String gridIndex) {
        if (gridIndexToTrackOffset.containsKey(gridIndex)) {
            return gridIndexToTrackOffset.get(gridIndex);
        }
        return 0.0;
    }

    @Override
    protected List<String> getFileNames(Manifest manifest) {
        return manifest.getFileNames("");
    }

    @Override
    protected void setBandGeoCodings(Product targetProduct) throws IOException {
        final Band[] bands = targetProduct.getBands();
        for (Band band : bands) {
            final GeoCoding bandGeoCoding = getBandGeoCoding(targetProduct, getGridIndex(band.getName()));
            band.setGeoCoding(bandGeoCoding);
        }
        final ProductNodeGroup<Mask> maskGroup = targetProduct.getMaskGroup();
        for (int i = 0; i < maskGroup.getNodeCount(); i++) {
            final Mask mask = maskGroup.get(i);
            final GeoCoding bandGeoCoding = getBandGeoCoding(targetProduct, getGridIndex(mask.getName()));
            mask.setGeoCoding(bandGeoCoding);
        }
    }

    protected void addDataNodes(Product masterProduct, Product targetProduct) throws IOException {
        for (final Product sourceProduct : getOpenProductList()) {
            String gridIndex = getGridIndex(sourceProduct.getName());  // one of fn, an, bn, in, tn, tx
            final Map<String, String> mapping = new HashMap<>();
            for (final Band sourceBand : sourceProduct.getBands()) {
                final String sourceBandName = sourceBand.getName();
                if (sourceBandName.contains("orphan")) {
                    continue;
                }

                if (sourceBand instanceof SparseDataBand) {
                    final String targetBandName = getTargetBandName(gridIndex, sourceBandName);
                    sourceBand.setName(targetBandName);
                    targetProduct.addBand(sourceBand);
                    continue;
                }

                RasterDataNode targetNode = null;
                if (isNodeSpecial(sourceBand, targetProduct)) {
                    targetNode = addSpecialNode(gridIndex, sourceBand, targetProduct);
                } else {
                    final String targetBandName = getTargetBandName(gridIndex, sourceBandName);
                    if (!targetProduct.containsBand(targetBandName)) {
                        targetNode = ProductUtils.copyBand(sourceBandName, sourceProduct, targetBandName, targetProduct, true);
                    }
                }
                if (targetNode != null) {
                    configureTargetNode(sourceBand, targetNode);
                    mapping.put(sourceBandName, targetNode.getName());
                }
            }
            copyMasks(sourceProduct, targetProduct, mapping);
        }
    }

    protected RasterDataNode addSpecialNode(String gridIndex, Band sourceBand, Product targetProduct) {
        final String sourceBandName = sourceBand.getName();
        final String targetBandName = getTargetBandName(gridIndex, sourceBandName);

        final Double sourceStartOffset = getStartOffset(gridIndex);
        final Double sourceTrackOffset = getTrackOffset(gridIndex);
        if (sourceStartOffset == null || sourceTrackOffset == null) {
            return ProductUtils.copyBand(sourceBandName, sourceBand.getProduct(), targetBandName, targetProduct, true);
        }

        final short[] sourceResolutions = getResolutions(gridIndex);
        if (gridIndex.startsWith("t")) {
            return copyTiePointGrid(sourceBand, targetProduct, sourceStartOffset, sourceTrackOffset, sourceResolutions);
        }

        final Band targetBand = new Band(targetBandName, sourceBand.getDataType(),
                sourceBand.getRasterWidth(), sourceBand.getRasterHeight());
        targetProduct.addBand(targetBand);

        updateFlagCodingNamesFRP(sourceBand);   // to ensure that flag codings named "flag" don't overwrite each other tb 2024-05-03
        ProductUtils.copyRasterDataNodeProperties(sourceBand, targetBand);

        final AffineTransform imageToModelTransform = new AffineTransform();
        final float[] offsets = getOffsets(sourceStartOffset, sourceTrackOffset, sourceResolutions);
        imageToModelTransform.translate(offsets[0], offsets[1]);

        final short[] referenceResolutions = getReferenceResolutions();
        final double subSamplingX = ((double) sourceResolutions[0]) / referenceResolutions[0];
        final double subSamplingY = ((double) sourceResolutions[1]) / referenceResolutions[1];
        imageToModelTransform.scale(subSamplingX, subSamplingY);

        final RenderedImage sourceRenderedImage = sourceBand.getSourceImage().getImage(0);
        final DefaultMultiLevelModel targetModel =
                new DefaultMultiLevelModel(imageToModelTransform,
                        sourceRenderedImage.getWidth(), sourceRenderedImage.getHeight());
        final DefaultMultiLevelSource targetMultiLevelSource =
                new DefaultMultiLevelSource(sourceRenderedImage, targetModel);
        targetBand.setSourceImage(new DefaultMultiLevelImage(targetMultiLevelSource));

        return targetBand;
    }

    // package access for testing only tb 2024-05-03
    static void updateFlagCodingNamesFRP(Band sourceBand) {
        final String sourceProductname = sourceBand.getProduct().getName();
        final String flagCodingName;

        if (sourceProductname.contains("FRP_in")) {
            flagCodingName = "flags_in";
        } else if (sourceProductname.contains("FRP_an") || sourceProductname.contains("FRP_bn")) {
            flagCodingName = "flags_an_bn";
        } else {
            return; // no need to rename something tb 2024-05-03
        }

        final FlagCoding flagCoding = sourceBand.getFlagCoding();
        flagCoding.setName(flagCodingName);
    }

    // package access for testing only tb 2024-05-03
    static String getTargetBandName(String gridIndex, String sourceBandName) {
        return sourceBandName.endsWith("_" + gridIndex)
                ? sourceBandName
                : sourceBandName + "_" + gridIndex;
    }

    @Override
    protected void setGeoCoding(Product targetProduct) throws IOException {
        final LonLatNames result = getLonLatNames("in");
        if (result == null) {
            return ;
        }

        final Band lonBand = targetProduct.getBand(result.lonVariableName);
        final Band latBand = targetProduct.getBand(result.latVariableName);

        if (latBand != null && lonBand != null) {
            final ComponentGeoCoding geoCoding = createPixelGeoCoding(targetProduct, lonBand, latBand, result);
            geoCodingMap.put("in", geoCoding);
            targetProduct.setSceneGeoCoding(geoCoding);
        }
    }

    private GeoCoding getBandGeoCoding(Product product, String ending) throws IOException {
        if (geoCodingMap.containsKey(ending)) {
            return geoCodingMap.get(ending);
        }

        final LonLatNames result = getLonLatNames(ending);
        if (result == null) {
            return null;
        }

        // for the an/bn rasters, there is no geo-coding supplied with the data. As discussed with OPTMPC, we interpolate
        // the 500m data geo-location from the 1 km in raster. tb 2024-06-06
        if ("an".equals(ending) || "bn".equals(ending)) {
            // which we can safely call, because it is added first as the reference geo-coding for the product tb 2024-05-06
            final ComponentGeoCoding inGeoCoding = (ComponentGeoCoding) geoCodingMap.get("in");
            final GeoRaster inGeoRaster = inGeoCoding.getGeoRaster();
            final int sceneRasterWidth = product.getSceneRasterWidth();
            final int sceneRasterHeight = product.getSceneRasterHeight();
            final GeoRaster geoRaster = new GeoRaster(inGeoRaster.getLongitudes(), inGeoRaster.getLatitudes(),
                    result.lonVariableName, result.latVariableName,
                    sceneRasterWidth, sceneRasterHeight,
                    sceneRasterWidth * 2, sceneRasterHeight * 2, RESOLUTION_IN_KM * 0.5,
                    -0.25, -0.25,
                    2, 2);
            final ForwardCoding forward = ComponentFactory.getForward(TiePointBilinearForward.KEY);
            final InverseCoding inverse = ComponentFactory.getInverse(TiePointInverse.KEY);
            final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
            geoCoding.initialize();
            geoCodingMap.put("an", geoCoding);
            geoCodingMap.put("bn", geoCoding);
            return geoCoding;
        }

        final Band lonBand = product.getBand(result.lonVariableName);
        final Band latBand = product.getBand(result.latVariableName);

        if (latBand != null && lonBand != null) {
            final ComponentGeoCoding geoCoding = createPixelGeoCoding(product, lonBand, latBand, result);
            geoCodingMap.put(ending, geoCoding);
            return geoCoding;
        }
        return null;
    }

    private static ComponentGeoCoding createPixelGeoCoding(Product product, Band lonBand, Band latBand, LonLatNames result) throws IOException {
        final double[] longitudes = RasterUtils.loadGeoData(lonBand);
        final double[] latitudes = RasterUtils.loadGeoData(latBand);

        final int sceneRasterWidth = product.getSceneRasterWidth();
        final int sceneRasterHeight = product.getSceneRasterHeight();
        final GeoRaster geoRaster = new GeoRaster(longitudes, latitudes, result.lonVariableName, result.latVariableName,
                sceneRasterWidth, sceneRasterHeight, RESOLUTION_IN_KM);

        final Preferences preferences = Config.instance("opttbx").preferences();
        final String inverseKey = preferences.get(SYSPROP_SLSTR_FRP_PIXEL_CODING_INVERSE, PixelQuadTreeInverse.KEY);
        final String[] keys = getForwardAndInverseKeys_pixelCoding(inverseKey);
        final ForwardCoding forward = ComponentFactory.getForward(keys[0]);
        final InverseCoding inverse = ComponentFactory.getInverse(keys[1]);

        final ComponentGeoCoding geoCoding = new ComponentGeoCoding(geoRaster, forward, inverse, GeoChecks.POLES);
        geoCoding.initialize();
        return geoCoding;
    }

    // package access for testing only tb 2024-05-03
     static LonLatNames getLonLatNames(String end) {
        String lonVariableName;
        String latVariableName;
        switch (end) {
            case "in":
            case "an":
            case "bn":
                lonVariableName = "longitude_in";
                latVariableName = "latitude_in";
                break;
            case "fn":
                lonVariableName = "longitude_fn";
                latVariableName = "latitude_fn";
                break;
            default:
                return null;
        }
        return new LonLatNames(lonVariableName, latVariableName);
    }

    static class LonLatNames {
        public final String lonVariableName;
        public final String latVariableName;

        LonLatNames(String lonVariableName, String latVariableName) {
            this.lonVariableName = lonVariableName;
            this.latVariableName = latVariableName;
        }
    }

    @Override
    protected void setAutoGrouping(Product[] sourceProducts, Product targetProduct) {
        targetProduct.setAutoGrouping("in/*_in:an/*_an:bn/*_bn");
    }
}
