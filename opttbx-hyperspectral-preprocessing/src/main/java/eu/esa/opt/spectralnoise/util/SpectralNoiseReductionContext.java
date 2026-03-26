package eu.esa.opt.spectralnoise.util;

import org.esa.snap.core.gpf.Tile;

import java.awt.*;


public class SpectralNoiseReductionContext {


    private final Rectangle targetRectangle;
    private final Tile[] sourceTiles;
    private final Tile[] requestedTargetTiles;
    private final double[][] sourceSamples;
    private final double[][] targetSamples;
    private final double[] noDataValues;
    private final boolean[] noDataUsed;
    private final double[] kernel;
    private final int bandCount;
    private final int tileWidth;
    private final int tileHeight;


    public SpectralNoiseReductionContext(Rectangle targetRectangle,
                                   Tile[] sourceTiles,
                                   Tile[] requestedTargetTiles,
                                   double[][] sourceSamples,
                                   double[][] targetSamples,
                                   double[] noDataValues,
                                   boolean[] noDataUsed,
                                   double[] kernel) {
        this.targetRectangle = targetRectangle;
        this.sourceTiles = sourceTiles;
        this.requestedTargetTiles = requestedTargetTiles;
        this.sourceSamples = sourceSamples;
        this.targetSamples = targetSamples;
        this.noDataValues = noDataValues;
        this.noDataUsed = noDataUsed;
        this.kernel = kernel;
        this.bandCount = sourceTiles.length;
        this.tileWidth = targetRectangle.width;
        this.tileHeight = targetRectangle.height;
    }

    public Rectangle getTargetRectangle() {
        return targetRectangle;
    }

    public Tile[] getSourceTiles() {
        return sourceTiles;
    }

    public Tile[] getRequestedTargetTiles() {
        return requestedTargetTiles;
    }

    public double[][] getSourceSamples() {
        return sourceSamples;
    }

    public double[][] getTargetSamples() {
        return targetSamples;
    }

    public double[] getNoDataValues() {
        return noDataValues;
    }

    public boolean[] getNoDataUsed() {
        return noDataUsed;
    }

    public double[] getKernel() {
        return kernel;
    }

    public int getBandCount() {
        return bandCount;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
}
