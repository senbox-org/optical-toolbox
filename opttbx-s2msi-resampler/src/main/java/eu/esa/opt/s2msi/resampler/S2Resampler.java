package eu.esa.opt.s2msi.resampler;

import com.bc.ceres.multilevel.MultiLevelImage;
import com.bc.ceres.multilevel.MultiLevelModel;
import com.bc.ceres.multilevel.support.DefaultMultiLevelImage;
import com.bc.ceres.multilevel.support.DefaultMultiLevelModel;
import com.bc.ceres.multilevel.support.DefaultMultiLevelSource;
import eu.esa.opt.dataio.s2.S2BandAnglesGrid;
import eu.esa.opt.dataio.s2.S2BandAnglesGridByDetector;
import eu.esa.opt.dataio.s2.S2BandConstants;
import eu.esa.opt.dataio.s2.ortho.S2AnglesGeometry;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.common.SubsetOp;
import org.esa.snap.core.gpf.common.resample.ResamplingOp;
import org.esa.snap.core.util.jai.JAIUtils;
import org.esa.snap.core.util.ProductUtils;
import org.opengis.referencing.operation.MathTransform;

import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.AddCollectionDescriptor;
import javax.media.jai.operator.MultiplyConstDescriptor;
import javax.media.jai.operator.MultiplyDescriptor;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.*;

/**
 * Created by obarrile on 15/05/2017.
 */
public class S2Resampler implements Resampler {

    public static final int DEFAULT_JAI_TILE_SIZE = 512;
    private static final double[] MULTIPLICATION_CONST = new double[] { 1/255.0 };

    static final String S2RESAMPLER_NAME = "S2Resampler";
    static final String S2RESAMPLER_DESCRIPTION = "S2Resampler applies a NearestNeighbour to reflectance bands " +
            "and Bilinear to angles (considering the detector footprint for the angles)";

    private int referenceWidth;
    private int referenceHeight;
    private MultiLevelModel referenceMultiLevelModel;

    private int targetWidth = 0;
    private int targetHeight = 0;
    private int targetResolution = 0;
    private String referenceBandName = null;
    private Dimension referenceTileSize;


    private String upsamplingMethod = "Bilinear";
    private String downsamplingMethod = "Mean";
    private String flagDownsamplingMethod = "First";
    private boolean resampleOnPyramidLevels = true;

    private String[] bandFilter;

    private String[] maskFilter;

    private final ArrayList<S2BandConstants> listUpdatedBands = new ArrayList<>(17);

    public S2Resampler(String referenceBandName) {
        this.referenceBandName = referenceBandName;
    }

    public S2Resampler(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    public S2Resampler(int targetResolution) {
        this.targetResolution = targetResolution;
    }


    public ArrayList<S2BandConstants> getListUpdatedBands() {
        return this.listUpdatedBands;
    }

    public String getUpsamplingMethod() {
        return upsamplingMethod;
    }

    public void setUpsamplingMethod(String upsamplingMethod) {
        this.upsamplingMethod = upsamplingMethod;
    }

    public String getDownsamplingMethod() {
        return downsamplingMethod;
    }

    public void setDownsamplingMethod(String downsamplingMethod) {
        this.downsamplingMethod = downsamplingMethod;
    }

    public String getFlagDownsamplingMethod() {
        return flagDownsamplingMethod;
    }

    public void setFlagDownsamplingMethod(String flagDownsamplingMethod) {
        this.flagDownsamplingMethod = flagDownsamplingMethod;
    }

    public boolean isResampleOnPyramidLevels() {
        return resampleOnPyramidLevels;
    }

    public void setResampleOnPyramidLevels(boolean resampleOnPyramidLevels) {
        this.resampleOnPyramidLevels = resampleOnPyramidLevels;
    }

    public void setBandFilter(String[] bandFilter) {
        this.bandFilter = bandFilter;
    }

    public void setMaskFilter(String[]maskFilter) {
        this.maskFilter = maskFilter;
    }

    @Override
    public String getName() {
        return S2RESAMPLER_NAME;
    }

    @Override
    public String getDescription() {
        return S2RESAMPLER_DESCRIPTION;
    }

    @Override
    public boolean canResample(Product multiSizeProduct) {
        //check detector footprint
        String[] maskNames = multiSizeProduct.getMaskGroup().getNodeNames();
        if (S2ResamplerUtils.countMatches(maskNames, "detector_footprint-.*-\\d{2}") <= 0)
            return false;
        //todo check metadata
        //todo check band names
        //String[] bandNames = multiSizeProduct.getBandNames();
        //if ((S2ResamplerUtils.countMatches(bandNames,"view_azimuth_.*") <= 0) || (S2ResamplerUtils.countMatches(bandNames,"view_zenith_.*") <= 0))
        //    return false;
        //todo check referencebandName
        return multiSizeProduct.getProductReader() instanceof S2AnglesGeometry;
    }
    public Product initialize(Product sourceProduct) {
        Product targetProduct = new Product(sourceProduct.getName() + "_" + S2RESAMPLER_NAME, sourceProduct.getProductType(),
                                    targetWidth, targetHeight);
        ProductUtils.copyFlagCodings(sourceProduct, targetProduct);
        ProductUtils.copyIndexCodings(sourceProduct, targetProduct);
        ProductUtils.copyMetadata(sourceProduct, targetProduct);
        ProductUtils.copyTimeInformation(sourceProduct, targetProduct);
        targetProduct.setPreferredTileSize(referenceTileSize);
        return targetProduct;
    }

    @Override
    public Product resample(Product multiSizeProduct) {

        //Check if can resample
        if(!canResample(multiSizeProduct)) {
            return null;
        }

        //set reference values
        setReferenceValues(multiSizeProduct);

        HashMap<String, Product> sourceProducts = new HashMap<>();
        sourceProducts.put(multiSizeProduct.getName(), multiSizeProduct);
        HashMap<String, Object> parameters = new HashMap<>();
        OperatorSpi spi;
        Operator operator;
        Product subsetProduct = null;
        if ((this.bandFilter != null && this.bandFilter.length > 0) || (this.maskFilter != null && this.maskFilter.length > 0)) {
            //Use SubsetOp
            spi = new SubsetOp.Spi();
            parameters.put("copyMetadata", true);
            if(this.bandFilter != null && this.bandFilter.length > 0) {
                parameters.put("bandNames", this.bandFilter);
            }
            operator = spi.createOperator(parameters, sourceProducts);
            operator.setSourceProduct(multiSizeProduct);
            subsetProduct = operator.getTargetProduct();

            //initialize subset from source product
            ProductUtils.copyFlagCodings(multiSizeProduct, subsetProduct);
            ProductUtils.copyIndexCodings(multiSizeProduct, subsetProduct);

            //filter masks
            if (this.maskFilter != null && this.maskFilter.length > 0) {
                Mask[] sourceMasks = subsetProduct.getMaskGroup().toArray(new Mask[0]);
                subsetProduct.getMaskGroup().removeAll();
                List<String> targetMaskNames = Arrays.asList(this.maskFilter);
                //check if the removed masks are vector masks and remove them from the vector data too.
                for (Mask mask : sourceMasks) {
                    if(targetMaskNames.contains(mask.getName())){
                        subsetProduct.getMaskGroup().add(mask);
                    } else if (mask.getImageType().getName().equals(Mask.VectorDataType.TYPE_NAME)) {
                        VectorDataNode vectorMask = subsetProduct.getVectorDataGroup().get(mask.getName());
                        if(vectorMask != null) {
                            subsetProduct.getVectorDataGroup().remove(vectorMask);
                        }
                    }
                }
            }

            sourceProducts.clear();
            sourceProducts.put(subsetProduct.getName(), subsetProduct);
            parameters.clear();
        }

        //Use ResamplingOp
        spi = new ResamplingOp.Spi();

        if (referenceBandName == null && targetWidth == 0 && targetHeight == 0 && targetResolution == 0) {
            referenceBandName = subsetProduct != null ?  subsetProduct.getBandNames()[0] : multiSizeProduct.getBandNames()[0];
        }
        if (referenceBandName != null) {
            //check which one is the used
            parameters.put("referenceBand", referenceBandName);
            parameters.put("referenceBandName", referenceBandName);
        } else if (targetWidth != 0 && targetHeight != 0) {
            parameters.put("targetWidth", targetWidth);
            parameters.put("targetHeight", targetHeight);
        } else {
            parameters.put("targetResolution", targetResolution);
        }
        parameters.put("upsampling", upsamplingMethod);
        parameters.put("downsampling", downsamplingMethod);
        parameters.put("flagDownsampling", flagDownsamplingMethod);
        parameters.put("resampleOnPyramidLevels", resampleOnPyramidLevels);

        operator = spi.createOperator(parameters, sourceProducts);
        operator.setSourceProduct(subsetProduct != null ? subsetProduct : multiSizeProduct);
        Product targetProduct = operator.getTargetProduct();

        //Update the angle bands
        for (S2BandConstants bandConstants : S2BandConstants.values()) {
            if(updateAngleBands(multiSizeProduct, targetProduct, bandConstants)) {
                listUpdatedBands.add(bandConstants);
            }
        }
        //if no angle band selected, remove the mean angles
        if (listUpdatedBands.size() < 1 && targetProduct.getBandIndex("view_zenith_mean") >= 0){
            targetProduct.removeBand(targetProduct.getBand("view_zenith_mean"));
        }
        if (listUpdatedBands.size() < 1 && targetProduct.getBandIndex("view_azimuth_mean") >= 0){
            targetProduct.removeBand(targetProduct.getBand("view_azimuth_mean"));
        }

        //mean angles, computed only with the updated bands
        // replaceMeanAnglesBand(listUpdatedBands,targetProduct);

        //sun angles
        updateSolarAngles(multiSizeProduct, targetProduct);

        return targetProduct;
    }

    //TODO homogenize code (copied from resamplingOp)
    private void setReferenceValues(Product sourceProduct) {
        if (referenceBandName == null && targetWidth == 0 && targetHeight == 0 && targetResolution == 0) {
            referenceBandName = sourceProduct.getBandNames()[0];
        }
        AffineTransform referenceImageToModelTransform;
        if (referenceBandName != null) {
            final Band referenceBand = sourceProduct.getBand(referenceBandName);
            referenceWidth = referenceBand.getRasterWidth();
            referenceHeight = referenceBand.getRasterHeight();
            referenceImageToModelTransform = referenceBand.getImageToModelTransform();
            referenceMultiLevelModel = referenceBand.getMultiLevelModel();
        } else if (targetWidth != 0 && targetHeight != 0) {
            referenceWidth = targetWidth;
            referenceHeight = targetHeight;
            double scaleX = (double) sourceProduct.getSceneRasterWidth() / referenceWidth;
            double scaleY = (double) sourceProduct.getSceneRasterHeight() / referenceHeight;
            GeoCoding sceneGeoCoding = sourceProduct.getSceneGeoCoding();
            if (sceneGeoCoding != null && sceneGeoCoding.getImageToMapTransform() instanceof AffineTransform) {
                AffineTransform mapTransform = (AffineTransform) sceneGeoCoding.getImageToMapTransform();
                referenceImageToModelTransform =
                        new AffineTransform(scaleX * mapTransform.getScaleX(), 0, 0, scaleY * mapTransform.getScaleY(),
                                            mapTransform.getTranslateX(), mapTransform.getTranslateY());
            } else {
                referenceImageToModelTransform = new AffineTransform(scaleX, 0, 0, scaleY, 0, 0);
            }
            referenceMultiLevelModel = new DefaultMultiLevelModel(referenceImageToModelTransform, referenceWidth, referenceHeight);
        } else {
            final MathTransform imageToMapTransform = sourceProduct.getSceneGeoCoding().getImageToMapTransform();
            if (imageToMapTransform instanceof AffineTransform) {
                AffineTransform mapTransform = (AffineTransform) imageToMapTransform;
                referenceWidth = (int) (sourceProduct.getSceneRasterWidth() * Math.abs(mapTransform.getScaleX()) / targetResolution);
                referenceHeight = (int) (sourceProduct.getSceneRasterHeight() * Math.abs(mapTransform.getScaleY()) / targetResolution);
                referenceImageToModelTransform = new AffineTransform(targetResolution, 0, 0, -targetResolution,
                                                                     mapTransform.getTranslateX(), mapTransform.getTranslateY());
                referenceMultiLevelModel = new DefaultMultiLevelModel(referenceImageToModelTransform, referenceWidth, referenceHeight);
            } else {
                throw new IllegalArgumentException("Use of target resolution parameter is not possible for this source product.");
            }
        }
        referenceTileSize = sourceProduct.getPreferredTileSize();
        if (referenceTileSize == null) {
            referenceTileSize = JAIUtils.computePreferredTileSize(referenceWidth, referenceHeight, 1);
        }

    }

    private boolean updateAngleBands(Product multiSizeProduct, Product targetProduct, S2BandConstants bandConstants) {

        Vector<RenderedOp> inputsZenith = new Vector<>(17);
        Vector<RenderedOp> inputsAzimuth = new Vector<>(17);
        String azimuthAnglesBandName = String.format("view_azimuth_%s", bandConstants.getPhysicalName());
        String zenithAnglesBandName = String.format("view_zenith_%s", bandConstants.getPhysicalName());
        Band bandZenith = targetProduct.getBand(zenithAnglesBandName);
        Band bandAzimuth = targetProduct.getBand(azimuthAnglesBandName);
        if (bandAzimuth == null || bandZenith == null) {
            return false;
        }
        for (int detectorId = 1 ; detectorId <= 12 ; detectorId++) {
            String maskName = String.format("detector_footprint-%s-%02d", bandConstants.getFilenameBandId(), detectorId);
            String nextMaskName = String.format("detector_footprint-%s-%02d", bandConstants.getFilenameBandId(), detectorId+1);
            String bandMaskName =  String.format("B_detector_footprint_%s", bandConstants.getPhysicalName());

            if (multiSizeProduct.getMaskGroup().get(maskName) == null) {
                continue;
            }

            String maskExpression;
            if (multiSizeProduct.getMaskGroup().get(nextMaskName) == null) {
                maskExpression = String.format("'%s'>0", maskName);
            } else {
                maskExpression = String.format("'%s'>0 && '%s'==0", maskName, nextMaskName);
            }
            Band maskBandInfo = multiSizeProduct.getBand(bandMaskName);
            Band auxBand = null;
            if(maskBandInfo!=null) {
                auxBand = maskBandInfo;
            } else {
                auxBand = multiSizeProduct.getBand(bandConstants.getPhysicalName());
            }

            //To support MUSCATE products
            if( auxBand == null) {
                String[] bandNames = multiSizeProduct.getBandNames();
                for(String bandName : bandNames) {
                    if (bandName.endsWith(bandConstants.getPhysicalName()) && !bandName.startsWith("view") && !bandName.startsWith("sun")) {
                        auxBand = multiSizeProduct.getBand(bandName);
                        break;
                    }
                }
            }
            if (auxBand == null) {
                return false;
            }

            MultiLevelImage footprint = multiSizeProduct.getMaskImage(maskExpression, auxBand);
            MultiLevelImage footprintFinal = S2ResamplerUtils.createInterpolatedImage(footprint, 0.0f, auxBand.getImageToModelTransform(),
                                                                                      referenceWidth, referenceHeight, referenceTileSize,
                                                                                      referenceMultiLevelModel, S2ResamplerUtils.getInterpolation("Nearest"));


            S2BandAnglesGridByDetector[] anglesGridByDetector = ((S2AnglesGeometry) multiSizeProduct.getProductReader()).getViewingIncidenceAnglesGrids(bandConstants.getBandIndex(), detectorId);
            float[] extendedZenithData = S2ResamplerUtils.extendDataV2(anglesGridByDetector[0].getData(),
                                                                       anglesGridByDetector[0].getWidth(),
                                                                       anglesGridByDetector[0].getHeight());
            float[] extendedAzimuthData = S2ResamplerUtils.extendDataV2(anglesGridByDetector[1].getData(),
                                                                        anglesGridByDetector[1].getWidth(),
                                                                        anglesGridByDetector[1].getHeight());
            int extendedWidth = anglesGridByDetector[0].getWidth() + 2;
            int extendedHeight = anglesGridByDetector[0].getHeight() + 2;
            AffineTransform originalAffineTransform5000 = new AffineTransform(anglesGridByDetector[0].getResolutionX(), 0.0f, 0.0f,
                                                                              -anglesGridByDetector[0].getResolutionX(), anglesGridByDetector[0].originX, anglesGridByDetector[0].originY);
            AffineTransform extendedAffineTransform5000 = (AffineTransform) originalAffineTransform5000.clone();
            extendedAffineTransform5000.translate(-1d,-1d);
            MultiLevelImage zenithMultiLevelImage = S2ResamplerUtils.createMultiLevelImage(extendedZenithData, extendedWidth, extendedHeight, extendedAffineTransform5000);
            MultiLevelImage targetImageZenith = S2ResamplerUtils.createInterpolatedImage(zenithMultiLevelImage, 0.0f, extendedAffineTransform5000,
                                                                                          referenceWidth, referenceHeight, referenceTileSize,
                                                                                         referenceMultiLevelModel, S2ResamplerUtils.getInterpolation("Bilinear"));
            MultiLevelImage azimuthMultiLevelImage = S2ResamplerUtils.createMultiLevelImage(extendedAzimuthData, extendedWidth, extendedHeight, extendedAffineTransform5000);
            MultiLevelImage targetImageAzimuth = S2ResamplerUtils.createInterpolatedImage(azimuthMultiLevelImage, 0.0f, extendedAffineTransform5000,
                                                                                          referenceWidth, referenceHeight, referenceTileSize,
                                                                                          referenceMultiLevelModel, S2ResamplerUtils.getInterpolation("Bilinear"));

            ImageLayout imageLayout = new ImageLayout();
            imageLayout.setMinX(0);
            imageLayout.setMinY(0);
            imageLayout.setTileWidth(DEFAULT_JAI_TILE_SIZE);
            imageLayout.setTileHeight(DEFAULT_JAI_TILE_SIZE);
            imageLayout.setTileGridXOffset(0);
            imageLayout.setTileGridYOffset(0);

            RenderingHints hints=new RenderingHints(JAI.KEY_TILE_CACHE, JAI.getDefaultInstance().getTileCache());
            hints.put(JAI.KEY_IMAGE_LAYOUT, imageLayout);

            RenderedOp multiZenith = MultiplyDescriptor.create(targetImageZenith.getImage(0),
                                                               footprintFinal.getImage(0), hints);
            multiZenith = MultiplyConstDescriptor.create(multiZenith, MULTIPLICATION_CONST, hints);

            RenderedOp multiAzimuth = MultiplyDescriptor.create(targetImageAzimuth.getImage(0),
                                                                footprintFinal.getImage(0), hints);
            multiAzimuth = MultiplyConstDescriptor.create(multiAzimuth, MULTIPLICATION_CONST, hints);

            inputsZenith.add(multiZenith);
            inputsAzimuth.add(multiAzimuth);
        }
        if(inputsAzimuth.isEmpty() || inputsZenith.isEmpty()) {
            return false;
        }
        ImageLayout imageLayout = new ImageLayout();
        imageLayout.setMinX(0);
        imageLayout.setMinY(0);
        imageLayout.setTileWidth(DEFAULT_JAI_TILE_SIZE);
        imageLayout.setTileHeight(DEFAULT_JAI_TILE_SIZE);
        imageLayout.setTileGridXOffset(0);
        imageLayout.setTileGridYOffset(0);

        RenderingHints hints=new RenderingHints(JAI.KEY_TILE_CACHE, JAI.getDefaultInstance().getTileCache());
        hints.put(JAI.KEY_IMAGE_LAYOUT, imageLayout);
        RenderedOp finalAnglesZenith;
        RenderedOp finalAnglesAzimuth;
        if (inputsZenith.size() > 1) {
            finalAnglesZenith = AddCollectionDescriptor.create(inputsZenith, hints);
        } else {
            finalAnglesZenith = inputsZenith.firstElement();
        }
        if (inputsAzimuth.size() > 1) {
            finalAnglesAzimuth = AddCollectionDescriptor.create(inputsAzimuth, hints);
        } else {
            finalAnglesAzimuth = inputsAzimuth.firstElement();
        }

        MultiLevelImage finalImageZenith = new DefaultMultiLevelImage(
                new DefaultMultiLevelSource(finalAnglesZenith, referenceMultiLevelModel, Interpolation.getInstance(Interpolation.INTERP_NEAREST)));
        MultiLevelImage finalImageAzimuth = new DefaultMultiLevelImage(
                new DefaultMultiLevelSource(finalAnglesAzimuth, referenceMultiLevelModel, Interpolation.getInstance(Interpolation.INTERP_NEAREST)));

        bandZenith.setSourceImage(S2ResamplerUtils.adjustImageToModelTransform(finalImageZenith, referenceMultiLevelModel));
        bandAzimuth.setSourceImage(S2ResamplerUtils.adjustImageToModelTransform(finalImageAzimuth, referenceMultiLevelModel));
        return true;
    }


    public void updateSolarAngles(Product multiSizeProduct, Product targetProduct) {
        String azimuthBandName = "sun_azimuth";
        String zenithBandName = "sun_zenith";
        Band bandZenith = targetProduct.getBand(zenithBandName);
        Band bandAzimuth = targetProduct.getBand(azimuthBandName);

        if (bandZenith != null || bandAzimuth != null) {
            S2BandAnglesGrid[] anglesGrid = ((S2AnglesGeometry) multiSizeProduct.getProductReader()).getSunAnglesGrid();
            float[] extendedZenithData = S2ResamplerUtils.extendDataV2(anglesGrid[0].getData(),
                                                                       anglesGrid[0].getWidth(),
                                                                       anglesGrid[0].getHeight());
            float[] extendedAzimuthData = S2ResamplerUtils.extendDataV2(anglesGrid[1].getData(),
                                                                        anglesGrid[1].getWidth(),
                                                                        anglesGrid[1].getHeight());
            int extendedWidth = anglesGrid[0].getWidth() + 2;
            int extendedHeight = anglesGrid[0].getHeight() + 2;
            AffineTransform originalAffineTransform5000 = new AffineTransform(anglesGrid[0].getResolutionX(), 0.0f, 0.0f,
                                                                              -anglesGrid[0].getResolutionX(), anglesGrid[0].originX, anglesGrid[0].originY);
            AffineTransform extendedAffineTransform5000 = (AffineTransform) originalAffineTransform5000.clone();
            extendedAffineTransform5000.translate(-1d, -1d);
            MultiLevelImage zenithMultiLevelImage = S2ResamplerUtils.createMultiLevelImage(extendedZenithData, extendedWidth, extendedHeight, extendedAffineTransform5000);
            MultiLevelImage targetImageZenith = S2ResamplerUtils.createInterpolatedImage(zenithMultiLevelImage, 0.0f, extendedAffineTransform5000,
                                                                                         referenceWidth, referenceHeight, referenceTileSize,
                                                                                         referenceMultiLevelModel, S2ResamplerUtils.getInterpolation("Bilinear"));
            MultiLevelImage azimuthMultiLevelImage = S2ResamplerUtils.createMultiLevelImage(extendedAzimuthData, extendedWidth, extendedHeight, extendedAffineTransform5000);
            MultiLevelImage targetImageAzimuth = S2ResamplerUtils.createInterpolatedImage(azimuthMultiLevelImage, 0.0f, extendedAffineTransform5000,
                                                                                          referenceWidth, referenceHeight, referenceTileSize,
                                                                                          referenceMultiLevelModel, S2ResamplerUtils.getInterpolation("Bilinear"));
            if (bandZenith != null) {
                bandZenith.setSourceImage(S2ResamplerUtils.adjustImageToModelTransform(targetImageZenith, referenceMultiLevelModel));
            }
            if (bandAzimuth != null) {
                bandAzimuth.setSourceImage(S2ResamplerUtils.adjustImageToModelTransform(targetImageAzimuth, referenceMultiLevelModel));
            }
        }
    }

}
