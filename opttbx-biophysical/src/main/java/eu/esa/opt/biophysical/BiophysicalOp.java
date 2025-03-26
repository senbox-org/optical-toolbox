/*
 * Copyright (C) 2012 CSSI (foss-contact@c-s.fr)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package eu.esa.opt.biophysical;

import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.Mask;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.core.datamodel.SampleCoding;
import org.esa.snap.core.datamodel.TiePointGrid;
import org.esa.snap.core.datamodel.VirtualBand;
import org.esa.snap.core.gpf.GPF;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.pointop.PixelOperator;
import org.esa.snap.core.gpf.pointop.ProductConfigurer;
import org.esa.snap.core.gpf.pointop.Sample;
import org.esa.snap.core.gpf.pointop.SourceSampleConfigurer;
import org.esa.snap.core.gpf.pointop.TargetSampleConfigurer;
import org.esa.snap.core.gpf.pointop.WritableSample;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.core.util.math.MathUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author Julien Malik (CS SI)
 */
@OperatorMetadata(
        alias = "BiophysicalOp",
        category = "Optical/Thematic Land Processing/Biophysical Processor (LAI, fAPAR...)",
        version = "1.0",
        description = "The 'Biophysical Processor' operator retrieves LAI from atmospherically corrected Sentinel-2 products",
        authors = "Julien Malik",
        copyright = "CS Group (foss-contact@c-s.fr)")
public class BiophysicalOp extends PixelOperator {

    private final Map<BiophysicalVariable, BiophysicalAlgo> algos = new HashMap<>();

    @SourceProduct(alias = "source", description = "The source product.")
    private Product sourceProduct;

    @Parameter(defaultValue = "S2A", label = "Sensor", description = "Sensor", valueSet = {"S2A", "S2B", "S2C"})
    private String sensor;

    @Parameter(alias = "resolution",
            label = "Output resolution (m)",
            description = "The output resolution.",
            valueSet = {"10", "20", "60"},
            defaultValue = "60"
    )
    private String targetResolution;

    @Parameter(defaultValue = "true", label = "Compute LAI", description = "Compute LAI (Leaf Area Index)")
    private boolean computeLAI;

    @Parameter(defaultValue = "true", label = "Compute FAPAR", description = "Compute FAPAR (Fraction of Absorbed Photosynthetically Active Radiation)")
    private boolean computeFapar;

    @Parameter(defaultValue = "true", label = "Compute FVC", description = "Compute FVC (Fraction of Vegetation Cover)")
    private boolean computeFcover;

    @Parameter(defaultValue = "true", label = "Compute Cab", description = "Compute Cab (Chlorophyll content in the leaf)")
    private boolean computeCab;

    @Parameter(defaultValue = "true", label = "Compute CWC", description = "Compute Cw (Canopy Water Content)")
    private boolean computeCw;

    private final Pattern S2L2APattern = Pattern.compile("S2[A-C]_MSIL2A_\\d{8}T\\d{6}_N\\d{4}_R\\d{3}_T\\d{2}\\w{3}_\\d{8}T\\d{6}");
    private final Pattern S2L1CPattern = Pattern.compile("S2[A-C]_MSIL1C_\\d{8}T\\d{6}_N\\d{4}_R\\d{3}_T\\d{2}\\w{3}_\\d{8}T\\d{6}");

    private boolean needsResample = false;
    private List<BiophysicalVariable> biophysicalVariables;

    /**
     * Configures all source samples that this operator requires for the computation of target samples.
     * Source sample are defined by using the provided {@link SourceSampleConfigurer}.
     * <p/>
     * <p/> The method is called by {@link #initialize()}.
     *
     * @param sampleConfigurer The configurer that defines the layout of a pixel.
     * @throws OperatorException If the source samples cannot be configured.
     */
    @Override
    protected void configureSourceSamples(SourceSampleConfigurer sampleConfigurer) throws OperatorException {
        sampleConfigurer.defineSample(L2BInput.B3.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B3));
        sampleConfigurer.defineSample(L2BInput.B4.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B4));
        sampleConfigurer.defineSample(L2BInput.B5.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B5));
        sampleConfigurer.defineSample(L2BInput.B6.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B6));
        sampleConfigurer.defineSample(L2BInput.B7.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B7));
        sampleConfigurer.defineSample(L2BInput.B8A.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B8A));
        sampleConfigurer.defineSample(L2BInput.B11.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B11));
        sampleConfigurer.defineSample(L2BInput.B12.getIndex(), findBandLikeS2(sourceProduct, S2BandConstant.B12));
        sampleConfigurer.defineSample(L2BInput.VIEW_ZENITH.getIndex(), L2BInput.VIEW_ZENITH.getBandName());
        sampleConfigurer.defineSample(L2BInput.SUN_ZENITH.getIndex(), L2BInput.SUN_ZENITH.getBandName());
        sampleConfigurer.defineSample(L2BInput.SUN_AZIMUTH.getIndex(), L2BInput.SUN_AZIMUTH.getBandName());
        sampleConfigurer.defineSample(L2BInput.VIEW_AZIMUTH.getIndex(), L2BInput.VIEW_AZIMUTH.getBandName());
    }

    @Override
    protected void prepareInputs() throws OperatorException {
        try {
            super.prepareInputs();
        } catch (OperatorException e) {
            // multi-size
            if (S2L2APattern.matcher(this.sourceProduct.getName()).find()) {
                needsResample = true;
            } else if (S2L1CPattern.matcher(this.sourceProduct.getName()).find()) {
                throw new OperatorException("The operator requires an atmospherically corrected product");
            } else {
                throw e;
            }
        }
        loadAuxData();
    }

    private void loadAuxData() throws OperatorException {
        BiophysicalModel model = BiophysicalModel.getBiophysicalModel(sensor);
        if(model == null) {
            throw new OperatorException("Biophysical model not found. Not valid sensor: " + sensor);
        }
        try {
            for (BiophysicalVariable biophysicalVariable : BiophysicalVariable.values()) {
                if (model.computesVariable(biophysicalVariable) && isComputed(biophysicalVariable)) {
                    algos.put(biophysicalVariable, new BiophysicalAlgo(BiophysicalAuxdata.makeBiophysicalAuxdata(biophysicalVariable, model)));
                }
            }
        } catch(IOException e) {
            throw new OperatorException(e.getMessage());
        }
    }

    private String[] getS2Bands() {
        final Set<String> bandNames = new LinkedHashSet<>() {{
            add("B3"); add("B4"); add("B5"); add("B6"); add("B7"); add("B8A"); add("B11"); add("B12");
        }};
        final List<String> bands = new ArrayList<>(bandNames);
        boolean hasDetectorBands = Arrays.stream(this.sourceProduct.getBandNames()).anyMatch(n -> n.startsWith("B_detector_footprint_\""));
        for (String bandName : bandNames) {
            if (hasDetectorBands) {
                bands.add("B_detector_footprint_" + bandName);
            }
            bands.add("view_azimuth_" + bandName);
            bands.add("view_zenith_" + bandName);
        }
        bands.add("view_zenith_mean");
        bands.add("sun_zenith");
        bands.add("sun_azimuth");
        bands.add("view_azimuth_mean");
        return bands.toArray(new String[0]);
    }

    /**
     * Configures all target samples computed by this operator.
     * Target samples are defined by using the provided {@link TargetSampleConfigurer}.
     * <p/>
     * <p/> The method is called by {@link #initialize()}.
     *
     * @param sampleConfigurer The configurer that defines the layout of a pixel.
     * @throws OperatorException If the target samples cannot be configured.
     */
    @Override
    protected void configureTargetSamples(TargetSampleConfigurer sampleConfigurer) throws OperatorException {
        int sampleIndex = 0;
        for (BiophysicalVariable biophysicalVariable : this.biophysicalVariables) {
            sampleConfigurer.defineSample(sampleIndex++, biophysicalVariable.getSampleName());
            sampleConfigurer.defineSample(sampleIndex++, biophysicalVariable.getSampleName() + "_flags");
        }
    }

    /**
     * Configures the target product via the given {@link ProductConfigurer}. Called by {@link #initialize()}.
     * <p/>
     * Client implementations of this method usually add product components to the given target product, such as
     * {@link Band bands} to be computed by this operator,
     * {@link VirtualBand virtual bands},
     * {@link Mask masks}
     * or {@link SampleCoding sample codings}.
     * <p/>
     * The default implementation retrieves the (first) source product and copies to the target product
     * <ul>
     * <li>the start and stop time by calling {@link ProductConfigurer#copyTimeCoding()},</li>
     * <li>all tie-point grids by calling {@link ProductConfigurer#copyTiePointGrids(String...)},</li>
     * <li>the geo-coding by calling {@link ProductConfigurer#copyGeoCoding()}.</li>
     * </ul>
     * <p/>
     * Clients that require a similar behaviour in their operator shall first call the {@code super} method
     * in their implementation.
     *
     * @param productConfigurer The target product configurer.
     * @throws OperatorException If the target product cannot be configured.
     * @see Product#addBand(Band)
     * @see Product#addBand(String, String)
     * @see Product#addTiePointGrid(TiePointGrid)
     * @see Product#getMaskGroup()
     */
    @Override
    protected void configureTargetProduct(ProductConfigurer productConfigurer) {
        Product tp;
        double noDataValue;
        biophysicalVariables = Arrays.stream(BiophysicalVariable.values())
                                     .filter(v -> BiophysicalModel.S2A.computesVariable(v) && isComputed(v))
                                     .collect(Collectors.toList());
        if (needsResample) {
            HashMap<String, Product> sourceProducts = new HashMap<>();
            sourceProducts.put("sourceProduct", this.sourceProduct);
            final HashMap<String, Object> params = new HashMap<>();
            // Only resample selected bands
            final String[] s2Bands = getS2Bands();
            params.put("bands", s2Bands);
            params.put("targetResolution", targetResolution);
            noDataValue = this.sourceProduct.getBand(s2Bands[0]).getNoDataValue();
            // The new source product is the resampled one, we need to reset references
            this.sourceProduct = GPF.createProduct("S2Resampling", params, sourceProducts);
            // Dirty hack, but no other way to clear productList and productMap from OperatorContext
            setSourceProducts();
            setSourceProduct("source", this.sourceProduct);
            // We need to recreate the target product since scene dimensions may have changed from the initial one
            tp = createTargetProduct();
            ProductUtils.copyGeoCoding(sourceProduct, tp);
            ProductUtils.copyTimeInformation(sourceProduct, tp);
            setTargetProduct(tp);
            productConfigurer.setSourceProduct(this.sourceProduct);
            needsResample = false;
        } else {
            tp = productConfigurer.getTargetProduct();
            noDataValue = this.sourceProduct.getBand(getS2Bands()[0]).getNoDataValue();
        }
        super.configureTargetProduct(productConfigurer);
        productConfigurer.copyMetadata();
        productConfigurer.copyMasks();

        final List<String> description = new ArrayList<>();
        for (BiophysicalVariable biophysicalVariable : this.biophysicalVariables) {
            // Add biophysical variable band
            final Band biophysicalVariableBand = tp.addBand(biophysicalVariable.getBandName(), ProductData.TYPE_FLOAT32);
            biophysicalVariableBand.setDescription(biophysicalVariable.getDescription());
            biophysicalVariableBand.setUnit(biophysicalVariable.getUnit());
            // todo better setDescription
            // todo setValidPixelExpression
            biophysicalVariableBand.setNoDataValue(noDataValue);
            biophysicalVariableBand.setNoDataValueUsed(true);

            // Add corresponding flag band
            String flagBandName = String.format("%s_flags", biophysicalVariable.getBandName());
            final Band biophysicalVariableFlagBand = tp.addBand(flagBandName, ProductData.TYPE_UINT8);
            final FlagCoding biophysicalVariableFlagCoding = new FlagCoding(flagBandName);
            for (BiophysicalFlag flagDef : BiophysicalFlag.values()) {
                biophysicalVariableFlagCoding.addFlag(flagDef.getName(), flagDef.getFlagValue(), flagDef.getDescription());
            }
            tp.getFlagCodingGroup().add(biophysicalVariableFlagCoding);
            biophysicalVariableFlagBand.setSampleCoding(biophysicalVariableFlagCoding);

            // Add a mask for each flag
            for (BiophysicalFlag flagDef : BiophysicalFlag.values()) {
                String maskName = String.format("%s_%s", biophysicalVariable.getBandName(), flagDef.getName().toLowerCase());
                tp.addMask(maskName,
                           String.format("%s.%s", flagBandName, flagDef.getName()),
                           flagDef.getDescription(),
                           flagDef.getColor(), flagDef.getTransparency());
            }
            description.add(biophysicalVariable.getDescription());
        }
        tp.setDescription("Biophysical variables (" + String.join(",", description));
    }

    /**
     * Computes the target samples from the given source samples.
     * <p/>
     * The number of source/target samples is the maximum defined sample index plus one. Source/target samples are defined
     * by using the respective sample configurer in the
     * {@link #configureSourceSamples(SourceSampleConfigurer) configureSourceSamples} and
     * {@link #configureTargetSamples(TargetSampleConfigurer) configureTargetSamples} methods.
     * Attempts to read from source samples or write to target samples at undefined sample indices will
     * cause undefined behaviour.
     *
     * @param x             The current pixel's X coordinate.
     * @param y             The current pixel's Y coordinate.
     * @param sourceSamples The source samples (= source pixel).
     * @param targetSamples The target samples (= target pixel).
     */
    @Override
    protected void computePixel(int x, int y, Sample[] sourceSamples, WritableSample[] targetSamples) {

        double[] input = new double[11];
        /*for (int i = 0; i < 10; ++i) {
            input[i] = sourceSamples[i].getDouble();
        }*/

        input[L2BInput.B3.getIndex()] = sourceSamples[L2BInput.B3.getIndex()].getDouble();
        input[L2BInput.B4.getIndex()] = sourceSamples[L2BInput.B4.getIndex()].getDouble();
        input[L2BInput.B5.getIndex()] = sourceSamples[L2BInput.B5.getIndex()].getDouble();
        input[L2BInput.B6.getIndex()] = sourceSamples[L2BInput.B6.getIndex()].getDouble();
        input[L2BInput.B7.getIndex()] = sourceSamples[L2BInput.B7.getIndex()].getDouble();
        input[L2BInput.B8A.getIndex()] = sourceSamples[L2BInput.B8A.getIndex()].getDouble();
        input[L2BInput.B11.getIndex()] = sourceSamples[L2BInput.B11.getIndex()].getDouble();
        input[L2BInput.B12.getIndex()] = sourceSamples[L2BInput.B12.getIndex()].getDouble();

        // cos(View_Zenith)
        input[8] = Math.cos(MathUtils.DTOR * sourceSamples[L2BInput.VIEW_ZENITH.getIndex()].getDouble());
        // cos(Sun_Zenith)
        input[9] = Math.cos(MathUtils.DTOR * sourceSamples[L2BInput.SUN_ZENITH.getIndex()].getDouble());
        // cos(Relative_Azimuth)
        input[10] = Math.cos(MathUtils.DTOR * (sourceSamples[L2BInput.SUN_AZIMUTH.getIndex()].getDouble() - sourceSamples[L2BInput.VIEW_AZIMUTH.getIndex()].getDouble()));

        int targetIndex = 0;
        for (BiophysicalVariable biophysicalVariable : this.biophysicalVariables) {
            BiophysicalAlgo algo = algos.get(biophysicalVariable);
            BiophysicalAlgo.Result result = algo.process(input);
            targetSamples[targetIndex++].set(result.getOutputValue());

            targetSamples[targetIndex].set(BiophysicalFlag.INPUT_OUT_OF_RANGE.getBitIndex(), result.isInputOutOfRange());
            targetSamples[targetIndex].set(BiophysicalFlag.OUTPUT_THRESHOLDED_TO_MIN_OUTPUT.getBitIndex(), result.isOutputThresholdedToMinOutput());
            targetSamples[targetIndex].set(BiophysicalFlag.OUTPUT_THRESHOLDED_TO_MAX_OUTPUT.getBitIndex(), result.isOutputThresholdedToMaxOutput());
            targetSamples[targetIndex].set(BiophysicalFlag.OUTPUT_TOO_LOW.getBitIndex(), result.isOutputTooLow());
            targetSamples[targetIndex++].set(BiophysicalFlag.OUTPUT_TOO_HIGH.getBitIndex(), result.isOutputTooHigh());
        }
    }

    private boolean isComputed(BiophysicalVariable biophysicalVariable) {
        switch (biophysicalVariable) {
            case LAI:
                return computeLAI;
            case LAI_Cab:
                return computeCab;
            case LAI_Cw:
                return computeCw;
            case FAPAR:
                return computeFapar;
            case FCOVER:
                return computeFcover;
            default:
                // this is a programming error
                throw new AssertionError(String.format("Wrong biophysical variable value %s", biophysicalVariable));
        }
    }

    static String findBandLikeS2(Product product, S2BandConstant s2Band) {
        return findWaveBand(product, true, s2Band.getWavelengthCentral(), s2Band.getBandwidth(), s2Band.getPhysicalName());
    }

    // package local for testing reasons only
    static String findWaveBand(Product product, boolean fail, double centralWavelength, double maxDeltaWavelength, String... bandNames) {
        Band[] bands = product.getBands();
        String bestBand = null;
        double minDelta = Double.MAX_VALUE;
        for (Band band : bands) {
            double bandWavelength = band.getSpectralWavelength();
            if (bandWavelength > 0.0) {
                double delta = Math.abs(bandWavelength - centralWavelength);
                if (delta < minDelta && delta <= maxDeltaWavelength) {
                    bestBand = band.getName();
                    minDelta = delta;
                }
            }
        }
        if (bestBand != null) {
            return bestBand;
        }
        for (String bandName : bandNames) {
            Band band = product.getBand(bandName);
            if (band != null) {
                return band.getName();
            }
        }
        if (fail) {
            throw new OperatorException("Missing band at " + centralWavelength + " nm");
        }
        return null;
    }

    public static class Spi extends OperatorSpi {
        public Spi() {
            super(BiophysicalOp.class);
        }
    }

    public enum L2BInput {
        B3(0, "B3"),
        B4(1, "B4"),
        B5(2, "B5"),
        B6(3, "B6"),
        B7(4, "B7"),
        B8A(5, "B8A"),
        B11(6, "B11"),
        B12(7, "B12"),
        VIEW_ZENITH(8, "view_zenith_mean"),
        SUN_ZENITH(9, "sun_zenith"),
        SUN_AZIMUTH(10, "sun_azimuth"),
        VIEW_AZIMUTH(11, "view_azimuth_mean");

        private final int index;
        private final String bandName;

        L2BInput(int index, String bandName) {
            this.index = index;
            this.bandName = bandName;
        }

        public int getIndex() {
            return this.index;
        }

        public String getBandName() {
            return this.bandName;
        }
    }

    public enum S2BandConstant {
    	// Updated min and max values in accordance with the table from
    	// https://sentinels.copernicus.eu/web/sentinel/technical-guides/sentinel-2-msi/mission-performance
        B1("B1", "B01", 0, 433, 453, 443),
        B2("B2", "B02", 1, 457.5, 522.5, 490),
        B3("B3", "B03", 2, 542.5, 577.5, 560),
        B4("B4", "B04", 3, 650, 680, 665),
        B5("B5", "B05", 4, 697.5, 712.5, 705),
        B6("B6", "B06", 5, 732.5, 747.5, 740),
        B7("B7", "B07", 6, 773, 793, 783),
        B8("B8", "B08", 7, 784.5, 899.5, 842),
        B8A("B8A", "B8A", 8, 855, 875, 865),
        B9("B9", "B09", 9, 935, 955, 945),
        B10("B10", "B10", 10, 1360, 1390, 1375),
        B11("B11", "B11", 11, 1565, 1655, 1610),
        B12("B12", "B12", 12, 2100, 2280, 2190);

        private final String physicalName;
        private final String filenameBandId;
        private final int bandIndex;
        private final double wavelengthMin;
        private final double wavelengthMax;
        private final double wavelengthCentral;

        S2BandConstant(String physicalName,
                        String filenameBandId,
                        int bandIndex,
                        double wavelengthMin,
                        double wavelengthMax,
                        double wavelengthCentral ) {
            this.physicalName = physicalName;
            this.filenameBandId = filenameBandId;
            this.bandIndex = bandIndex;
            this.wavelengthMin = wavelengthMin;
            this.wavelengthMax = wavelengthMax;
            this.wavelengthCentral = wavelengthCentral;
        }

        public String getPhysicalName() {
            return physicalName;
        }

        public String getFilenameBandId() {
            return filenameBandId;
        }

        public int getBandIndex() {
            return bandIndex;
        }

        public double getWavelengthMin() {
            return wavelengthMin;
        }

        public double getWavelengthMax() {
            return wavelengthMax;
        }

        public double getBandwidth() { return wavelengthMax - wavelengthMin; }

        public double getWavelengthCentral() {
            return wavelengthCentral;
        }
    }

}
