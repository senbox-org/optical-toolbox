package eu.esa.opt.spectralnoise.util;

import com.bc.ceres.annotation.STTM;
import org.esa.snap.core.gpf.Tile;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class SpectralNoiseReducerTest {


    private static final double DOUBLE_ERR = 1.0e-10;


    @Test
    @STTM("SNAP-4173")
    public void test_ApplyConvolutionComputesWeightedAverage() {
        double[] spectrum = {10.0, 20.0, 30.0};
        boolean[] validMask = {true, true, true};
        double[] kernel = {0.25, 0.5, 0.25};
        double[] result = new double[3];

        SpectralNoiseReducer.applyConvolution(spectrum, validMask, kernel, result);

        assertArrayEquals(new double[]{12.5, 20.0, 27.5}, result, DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ApplyConvolutionUsesClampAtEdges() {
        double[] spectrum = {10.0, 20.0, 30.0};
        boolean[] validMask = {true, true, true};
        double[] kernel = {0.25, 0.5, 0.25};
        double[] result = new double[3];

        SpectralNoiseReducer.applyConvolution(spectrum, validMask, kernel, result);

        assertEquals(12.5, result[0], DOUBLE_ERR);
        assertEquals(27.5, result[2], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ApplyConvolutionSetsNaNForInvalidCenterSample() {
        double[] spectrum = {10.0, 20.0, 30.0};
        boolean[] validMask = {true, false, true};
        double[] kernel = {0.25, 0.5, 0.25};
        double[] result = new double[3];

        SpectralNoiseReducer.applyConvolution(spectrum, validMask, kernel, result);

        assertEquals(10.0, result[0], DOUBLE_ERR);
        assertTrue(Double.isNaN(result[1]));
        assertEquals(30.0, result[2], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ApplyConvolutionSkipsInvalidNeighborSamplesAndRenormalizes() {
        double[] spectrum = {10.0, 20.0, 30.0};
        boolean[] validMask = {true, true, false};
        double[] kernel = {0.25, 0.5, 0.25};
        double[] result = new double[3];

        SpectralNoiseReducer.applyConvolution(spectrum, validMask, kernel, result);

        assertEquals(0.25 * 10.0 + 0.5 * 10.0 + 0.25 * 20.0, result[0], DOUBLE_ERR);
        assertEquals((0.25 * 10.0 + 0.5 * 20.0) / 0.75, result[1], DOUBLE_ERR);
        assertTrue(Double.isNaN(result[2]));
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ApplyConvolutionFallsBackToOriginalSpectrumWhenWeightSumIsZero() {
        double[] spectrum = {10.0, 20.0, 30.0};
        boolean[] validMask = {true, true, true};
        double[] kernel = {0.0, 0.0, 0.0};
        double[] result = new double[3];

        SpectralNoiseReducer.applyConvolution(spectrum, validMask, kernel, result);

        assertArrayEquals(spectrum, result, DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelReadsValidSamples() {
        Tile tile0 = mock(Tile.class);
        Tile tile1 = mock(Tile.class);

        when(tile0.isSampleValid(100, 200)).thenReturn(true);
        when(tile1.isSampleValid(100, 200)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile0, tile1},
                new Tile[]{null, null},
                new double[][]{{1.5}, {2.5}},
                new double[][]{null, null},
                new double[]{-9999.0, -9999.0},
                new boolean[]{true, true},
                new double[]{1.0}
        );

        double[] spectrum = new double[2];
        boolean[] validMask = new boolean[2];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 100, 200, 0, spectrum, validMask);

        assertArrayEquals(new double[]{1.5, 2.5}, spectrum, DOUBLE_ERR);
        assertArrayEquals(new boolean[]{true, true}, validMask);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelMarksSampleInvalidWhenTileSampleIsInvalid() {
        Tile tile0 = mock(Tile.class);
        Tile tile1 = mock(Tile.class);

        when(tile0.isSampleValid(3, 4)).thenReturn(false);
        when(tile1.isSampleValid(3, 4)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile0, tile1},
                new Tile[]{null, null},
                new double[][]{{11.0}, {22.0}},
                new double[][]{null, null},
                new double[]{-9999.0, -9999.0},
                new boolean[]{true, true},
                new double[]{1.0}
        );

        double[] spectrum = new double[2];
        boolean[] validMask = new boolean[2];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 3, 4, 0, spectrum, validMask);

        assertTrue(Double.isNaN(spectrum[0]));
        assertEquals(22.0, spectrum[1], DOUBLE_ERR);
        assertArrayEquals(new boolean[]{false, true}, validMask);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelMarksSampleInvalidWhenValueIsNaN() {
        Tile tile = mock(Tile.class);
        when(tile.isSampleValid(1, 2)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile},
                new Tile[]{null},
                new double[][]{{Double.NaN}},
                new double[][]{null},
                new double[]{-9999.0},
                new boolean[]{true},
                new double[]{1.0}
        );

        double[] spectrum = new double[1];
        boolean[] validMask = new boolean[1];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 1, 2, 0, spectrum, validMask);

        assertTrue(Double.isNaN(spectrum[0]));
        assertFalse(validMask[0]);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelMarksSampleInvalidWhenValueIsInfinite() {
        Tile tile = mock(Tile.class);
        when(tile.isSampleValid(1, 2)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile},
                new Tile[]{null},
                new double[][]{{Double.POSITIVE_INFINITY}},
                new double[][]{null},
                new double[]{-9999.0},
                new boolean[]{true},
                new double[]{1.0}
        );

        double[] spectrum = new double[1];
        boolean[] validMask = new boolean[1];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 1, 2, 0, spectrum, validMask);

        assertTrue(Double.isNaN(spectrum[0]));
        assertFalse(validMask[0]);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelMarksSampleInvalidWhenNoDataIsUsedAndValueMatchesNoData() {
        Tile tile = mock(Tile.class);
        when(tile.isSampleValid(1, 2)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile},
                new Tile[]{null},
                new double[][]{{-9999.0}},
                new double[][]{null},
                new double[]{-9999.0},
                new boolean[]{true},
                new double[]{1.0}
        );

        double[] spectrum = new double[1];
        boolean[] validMask = new boolean[1];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 1, 2, 0, spectrum, validMask);

        assertTrue(Double.isNaN(spectrum[0]));
        assertFalse(validMask[0]);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_ReadSpectrumAtPixelKeepsSampleValidWhenNoDataIsNotUsed() {
        Tile tile = mock(Tile.class);
        when(tile.isSampleValid(1, 2)).thenReturn(true);

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{tile},
                new Tile[]{null},
                new double[][]{{-9999.0}},
                new double[][]{null},
                new double[]{-9999.0},
                new boolean[]{false},
                new double[]{1.0}
        );

        double[] spectrum = new double[1];
        boolean[] validMask = new boolean[1];

        SpectralNoiseReducer.readSpectrumAtPixel(context, 1, 2, 0, spectrum, validMask);

        assertEquals(-9999.0, spectrum[0], DOUBLE_ERR);
        assertTrue(validMask[0]);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_WriteFilteredSpectrumAtPixelWritesFilteredValueForValidSample() {
        double[][] targetSamples = new double[][]{new double[1], new double[1]};

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{mock(Tile.class), mock(Tile.class)},
                new Tile[]{mock(Tile.class), mock(Tile.class)},
                new double[][]{{1.0}, {2.0}},
                targetSamples,
                new double[]{-9999.0, -9999.0},
                new boolean[]{true, true},
                new double[]{1.0}
        );

        double[] spectrum = {1.0, 2.0};
        boolean[] validMask = {true, true};
        double[] filteredSpectrum = {10.0, 20.0};

        SpectralNoiseReducer.writeFilteredSpectrumAtPixel(context, 0, spectrum, validMask, filteredSpectrum);

        assertEquals(10.0, targetSamples[0][0], DOUBLE_ERR);
        assertEquals(20.0, targetSamples[1][0], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_WriteFilteredSpectrumAtPixelWritesNoDataForInvalidSampleWhenNoDataIsUsed() {
        double[][] targetSamples = new double[][]{new double[1]};

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{mock(Tile.class)},
                new Tile[]{mock(Tile.class)},
                new double[][]{{1.0}},
                targetSamples,
                new double[]{-9999.0},
                new boolean[]{true},
                new double[]{1.0}
        );

        double[] spectrum = {123.0};
        boolean[] validMask = {false};
        double[] filteredSpectrum = {10.0};

        SpectralNoiseReducer.writeFilteredSpectrumAtPixel(context, 0, spectrum, validMask, filteredSpectrum);

        assertEquals(-9999.0, targetSamples[0][0], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_WriteFilteredSpectrumAtPixelWritesOriginalSpectrumForInvalidSampleWhenNoDataIsNotUsed() {
        double[][] targetSamples = new double[][]{new double[1]};

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{mock(Tile.class)},
                new Tile[]{mock(Tile.class)},
                new double[][]{{1.0}},
                targetSamples,
                new double[]{-9999.0},
                new boolean[]{false},
                new double[]{1.0}
        );

        double[] spectrum = {123.0};
        boolean[] validMask = {false};
        double[] filteredSpectrum = {10.0};

        SpectralNoiseReducer.writeFilteredSpectrumAtPixel(context, 0, spectrum, validMask, filteredSpectrum);

        assertEquals(123.0, targetSamples[0][0], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_WriteFilteredSpectrumAtPixelSkipsBandsWithoutTargetSampleArray() {
        double[][] targetSamples = new double[][]{null, new double[1]};

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{mock(Tile.class), mock(Tile.class)},
                new Tile[]{mock(Tile.class), mock(Tile.class)},
                new double[][]{{1.0}, {2.0}},
                targetSamples,
                new double[]{-9999.0, -9999.0},
                new boolean[]{true, true},
                new double[]{1.0}
        );

        double[] spectrum = {1.0, 2.0};
        boolean[] validMask = {true, true};
        double[] filteredSpectrum = {10.0, 20.0};

        SpectralNoiseReducer.writeFilteredSpectrumAtPixel(context, 0, spectrum, validMask, filteredSpectrum);

        assertNull(targetSamples[0]);
        assertEquals(20.0, targetSamples[1][0], DOUBLE_ERR);
    }

    @Test
    @STTM("SNAP-4173")
    public void test_WriteTargetTilesWritesOnlyRequestedTargetTiles() {
        Tile requestedTile = mock(Tile.class);
        Tile skippedTile = null;

        double[][] targetSamples = new double[][]{
                new double[]{1.0, 2.0},
                new double[]{3.0, 4.0}
        };

        SpectralNoiseReductionContext context = createContext(
                new Tile[]{mock(Tile.class), mock(Tile.class)},
                new Tile[]{requestedTile, skippedTile},
                new double[][]{{5.0, 6.0}, {7.0, 8.0}},
                targetSamples,
                new double[]{-9999.0, -9999.0},
                new boolean[]{true, true},
                new double[]{1.0}
        );

        SpectralNoiseReducer.writeTargetTiles(context);

        verify(requestedTile).setSamples(targetSamples[0]);
    }

    private static SpectralNoiseReductionContext createContext(Tile[] sourceTiles,
                                                               Tile[] requestedTargetTiles,
                                                               double[][] sourceSamples,
                                                               double[][] targetSamples,
                                                               double[] noDataValues,
                                                               boolean[] noDataUsed,
                                                               double[] kernel) {
        return new SpectralNoiseReductionContext(
                new Rectangle(0, 0, 1, 1),
                sourceTiles,
                requestedTargetTiles,
                sourceSamples,
                targetSamples,
                noDataValues,
                noDataUsed,
                kernel
        );
    }
}