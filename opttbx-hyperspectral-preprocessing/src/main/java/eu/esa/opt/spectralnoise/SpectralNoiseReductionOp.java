package eu.esa.opt.spectralnoise;

import com.bc.ceres.core.ProgressMonitor;
import eu.esa.opt.spectralnoise.util.SpectralNoiseUtils;
import eu.esa.opt.spectralnoise.util.SpectralNoiseReductionContext;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.StringUtils;
import org.esa.snap.speclib.util.noise.SpectralNoiseKernelFactory;
import org.esa.snap.speclib.util.noise.SpectralNoiseReducer;

import java.awt.*;
import java.util.*;
import java.util.List;


@OperatorMetadata(
        alias = "SpectralNoiseReduction",
        category = "Optical/Preprocessing/Hyperspectral",
        authors = "Benjamin Lutz",
        version = "1.0",
        copyright = "Copyright (C) 2026 by Brockmann Consult",
        description = "Reduces spectral noise in hyperspectral data by applying a one-dimensional convolution filter along the spectral axis.")
public class SpectralNoiseReductionOp extends Operator {


    @SourceProduct(alias = "source")
    private Product sourceProduct;

    @TargetProduct
    private Product targetProduct;

    @Parameter(description = "The list of source bands.", alias = "sourceBands",
            label = "Source Bands", rasterDataNodeType = Band.class)
    private String[] sourceBands = null;

    @Parameter(valueSet = {SpectralNoiseKernelFactory.FILTER_SG, SpectralNoiseKernelFactory.FILTER_GAUSSIAN, SpectralNoiseKernelFactory.FILTER_BOX},
            defaultValue = SpectralNoiseKernelFactory.FILTER_SG, label = "Filter Method")
    private String filterType = SpectralNoiseKernelFactory.FILTER_SG;


    @Parameter(label = "Kernel Size",
            description = "Odd filter window size along the spectral axis.",
            defaultValue = "11")
    private int kernelSize;

    @Parameter(label = "Gaussian Sigma",
            description = "Gaussian Sigma",
            defaultValue = "1.0")
    private double gaussianSigma;

    @Parameter(label = "Polynomial Order",
            description = "Savitzky-Golay polynomial order",
            defaultValue = "3")
    private int sgPolynomialOrder;


    private static final String PRODUCT_SUFFIX = "_SNR";
    private final Map<Band, Band> targetToSourceBandMap = new LinkedHashMap<>();
    private double[] kernel;


    @Override
    public void initialize() throws OperatorException {
        final SpectralNoiseKernelFactory kernelParams = new SpectralNoiseKernelFactory(filterType, kernelSize, gaussianSigma, sgPolynomialOrder);
        try {
            kernelParams.validateFilterParameters();
        } catch (IllegalArgumentException e) {
            throw new OperatorException(e.getMessage(), e);
        }

        getSourceBands();
        try {
            kernelParams.ensureKernelSize(sourceBands.length);
            kernel = kernelParams.createKernel();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new OperatorException(e.getMessage(), e);
        }
        createTargetProduct();
        ProductUtils.copyProductNodes(sourceProduct, targetProduct);
        addTargetBands();
    }

    @Override
    public void dispose() {
        super.dispose();
        targetToSourceBandMap.clear();
        kernel = null;
        sourceBands = null;
    }


    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles,
                                 Rectangle targetRectangle,
                                 ProgressMonitor pm) throws OperatorException {
        pm.beginTask("Computing spectral noise reduction", targetRectangle.height);
        try {
            final SpectralNoiseReductionContext context = createTileComputationContext(targetTiles, targetRectangle);
            processTileRectangle(context, pm);
            SpectralNoiseUtils.writeTargetTiles(context);
        } catch (OperatorException e) {
            throw e;
        } catch (Throwable t) {
            throw new OperatorException("Failed to compute spectral noise reduction.", t);
        } finally {
            pm.done();
        }
    }

    /**
     * Create target product.
     */
    private void createTargetProduct() {
        targetProduct = new Product(
                sourceProduct.getName() + PRODUCT_SUFFIX,
                sourceProduct.getProductType(),
                sourceProduct.getSceneRasterWidth(),
                sourceProduct.getSceneRasterHeight());
    }

    /**
     * Add bands to the target product.
     */
    private void addTargetBands() {
        for (String bandName : sourceBands) {
            Band srcBand = sourceProduct.getBand(bandName);
            Band targetBand = ProductUtils.copyBand(bandName, sourceProduct, bandName, targetProduct, false);

            targetToSourceBandMap.put(targetBand, srcBand);
        }
    }

    private void getSourceBands() {
        final List<String> srcBandNameList = new ArrayList<>();
        final Band[] srcBands = sourceProduct.getBands();

        if (sourceBands != null) {
            for (Band srcBand : srcBands) {
                if(StringUtils.contains(sourceBands, srcBand.getName()) && isSpectralBand(srcBand)) {
                    srcBandNameList.add(srcBand.getName());
                }
            }
        }

        if (srcBandNameList.isEmpty()) {
            for (Band srcBand : srcBands) {
                if (isSpectralBand(srcBand)) {
                    srcBandNameList.add(srcBand.getName());
                }
            }
        }

        sourceBands = srcBandNameList.toArray(new String[srcBandNameList.size()]);
    }

    private boolean isSpectralBand(Band band) {
        return band.getSpectralWavelength() != 0 && band.getSpectralWavelength() > 0;
    }


    private SpectralNoiseReductionContext createTileComputationContext(Map<Band, Tile> targetTiles, Rectangle targetRectangle) {
        final List<Map.Entry<Band, Band>> bandMappings = getOrderedBandMappings();
        if (bandMappings.isEmpty()) {
            throw new OperatorException("No spectral source bands available for filtering.");
        }

        final int bandCount = bandMappings.size();
        final int tileSize = targetRectangle.width * targetRectangle.height;

        final Tile[] sourceTiles = new Tile[bandCount];
        final Tile[] requestedTargetTiles = new Tile[bandCount];
        final double[][] sourceSamples = new double[bandCount][];
        final double[][] targetSamples = new double[bandCount][];
        final double[] noDataValues = new double[bandCount];
        final boolean[] noDataUsed = new boolean[bandCount];

        for (int i = 0; i < bandCount; i++) {
            final Map.Entry<Band, Band> entry = bandMappings.get(i);
            final Band targetBand = entry.getKey();
            final Band sourceBand = entry.getValue();

            sourceTiles[i] = getRequiredSourceTile(sourceBand, targetRectangle);
            requestedTargetTiles[i] = targetTiles.get(targetBand);

            sourceSamples[i] = sourceTiles[i].getSamplesDouble();
            if (sourceSamples[i].length != tileSize) {
                throw new OperatorException("Unexpected tile sample length for band '" + sourceBand.getName() + "'.");
            }

            if (requestedTargetTiles[i] != null) {
                targetSamples[i] = new double[tileSize];
            }

            noDataValues[i] = sourceBand.getGeophysicalNoDataValue();
            noDataUsed[i] = sourceBand.isNoDataValueUsed();
        }

        return new SpectralNoiseReductionContext(
                targetRectangle,
                sourceTiles,
                requestedTargetTiles,
                sourceSamples,
                targetSamples,
                noDataValues,
                noDataUsed,
                kernel
        );
    }

    private List<Map.Entry<Band, Band>> getOrderedBandMappings() {
        final List<Map.Entry<Band, Band>> bandMappings = new ArrayList<>(targetToSourceBandMap.entrySet());
        bandMappings.sort(Comparator.comparingDouble(entry -> entry.getValue().getSpectralWavelength()));
        return bandMappings;
    }

    private Tile getRequiredSourceTile(Band sourceBand, Rectangle targetRectangle) {
        final Tile sourceTile = getSourceTile(sourceBand, targetRectangle);
        if (sourceTile == null) {
            throw new OperatorException("Cannot get source tile for band '" + sourceBand.getName() + "'.");
        }
        return sourceTile;
    }

    private void processTileRectangle(SpectralNoiseReductionContext context, ProgressMonitor pm) {
        final double[] spectrum = new double[context.getBandCount()];
        final boolean[] validMask = new boolean[context.getBandCount()];
        final double[] filteredSpectrum = new double[context.getBandCount()];

        int tileIndex = 0;
        for (int y = 0; y < context.getTileHeight(); y++) {
            checkForCancellation();

            final int absoluteY = context.getTargetRectangle().y + y;
            for (int x = 0; x < context.getTileWidth(); x++, tileIndex++) {
                final int absoluteX = context.getTargetRectangle().x + x;

                SpectralNoiseUtils.readSpectrumAtPixel(context, absoluteX, absoluteY, tileIndex, spectrum, validMask);
                SpectralNoiseReducer.applyConvolution(spectrum, validMask, context.getKernel(), filteredSpectrum);
                SpectralNoiseUtils.writeFilteredSpectrumAtPixel(context, tileIndex, spectrum, validMask, filteredSpectrum);
            }

            pm.worked(1);
        }
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(SpectralNoiseReductionOp.class);
        }
    }
}
