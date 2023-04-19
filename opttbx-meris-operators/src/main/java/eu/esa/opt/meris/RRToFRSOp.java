/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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
package eu.esa.opt.meris;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.Band;
import org.esa.snap.core.datamodel.FlagCoding;
import org.esa.snap.core.datamodel.GeoCoding;
import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

import java.awt.Rectangle;

@OperatorMetadata(alias = "RRToFRS", internal = true)
public class RRToFRSOp extends Operator {

    private GeoCoding rrGeoCoding;
    private GeoCoding frsGeoCoding;

    @SourceProduct(alias = "frs")
    private Product frsProduct;
    @SourceProduct(alias = "rr")
    private Product rrProduct;
    @TargetProduct
    private Product targetProduct;

    @Override
    public void initialize() throws OperatorException {
        rrGeoCoding = rrProduct.getSceneGeoCoding();
        frsGeoCoding = frsProduct.getSceneGeoCoding();
        final int width = frsProduct.getSceneRasterWidth();
        final int height = frsProduct.getSceneRasterHeight();
        checkThatRRContainsFRSData(width, height);

        targetProduct = new Product("L1", "L1", width, height);

        Band[] srcBands = rrProduct.getBands();
        for (Band sourceBand : srcBands) {
            Band targetBand = targetProduct.addBand(sourceBand.getName(), sourceBand.getDataType());
            ProductUtils.copySpectralBandProperties(sourceBand, targetBand);
            targetBand.setDescription(sourceBand.getDescription());
            targetBand.setUnit(sourceBand.getUnit());
            targetBand.setScalingFactor(sourceBand.getScalingFactor());
            targetBand.setScalingOffset(sourceBand.getScalingOffset());
            targetBand.setLog10Scaled(sourceBand.isLog10Scaled());
            targetBand.setNoDataValueUsed(sourceBand.isNoDataValueUsed());
            targetBand.setNoDataValue(sourceBand.getNoDataValue());
            if (sourceBand.getFlagCoding() != null) {
                FlagCoding srcFlagCoding = sourceBand.getFlagCoding();
                ProductUtils.copyFlagCoding(srcFlagCoding, targetProduct);
                targetBand.setSampleCoding(targetProduct.getFlagCodingGroup().get(srcFlagCoding.getName()));
            }
        }
    }

    private void checkThatRRContainsFRSData(int width, int height) throws OperatorException {
        getRrPixelPos(0, 0);
        getRrPixelPos(0, height - 1);
        getRrPixelPos(width - 1, 0);
        getRrPixelPos(width - 1, height - 1);
    }

    private PixelPos getRrPixelPos(int x, int y) throws OperatorException {
        PixelPos frsPixelPos = new PixelPos(x, y);
        GeoPos geoPos = frsGeoCoding.getGeoPos(frsPixelPos, null);
        PixelPos rrPixelPos = rrGeoCoding.getPixelPos(geoPos, null);

        final int xrr = (int)Math.round(rrPixelPos.x);
        final int yrr = (int)Math.round(rrPixelPos.y);
        if (rrProduct.containsPixel(xrr, yrr)) {
            return rrPixelPos;
        } else {
            throw new OperatorException("RR product does not contain data for this coordinate: x=" + x + " y=" + y);
        }

    }

    @Override
    public void computeTile(Band band, Tile targetTile, ProgressMonitor pm) throws OperatorException {

        Rectangle frsRectangle = targetTile.getRectangle();
        Band rrSrcBand = rrProduct.getBand(band.getName());
        pm.beginTask("compute", frsRectangle.height);

        PixelPos rrPixelPos = getRrPixelPos(frsRectangle.x, frsRectangle.y);
        final int xStart = (int)Math.round(rrPixelPos.x);
        final int yStart = (int)Math.round(rrPixelPos.y);
        Rectangle rrRectangle = new Rectangle(xStart, yStart, frsRectangle.width / 4, frsRectangle.height / 4);
        rrRectangle.grow(4, 4);
        Rectangle sceneRectangle = new Rectangle(rrSrcBand.getRasterWidth(), rrSrcBand.getRasterHeight());
        rrRectangle = rrRectangle.intersection(sceneRectangle);

        Tile srcTile = getSourceTile(rrSrcBand, rrRectangle);

        try {
            int rrY = yStart;
            int iy = 0;
            for (int y = frsRectangle.y; y < frsRectangle.y + frsRectangle.height; y++) {
                int rrX = xStart;
                int ix = 0;
                for (int x = frsRectangle.x; x < frsRectangle.x + frsRectangle.width; x++) {
                    double d = srcTile.getSampleDouble(rrX, rrY);
                    targetTile.setSample(x, y, d);
                    if (ix < 3) {
                        ix++;
                    } else {
                        ix = 0;
                        rrX++;
                    }
                }
                if (iy < 3) {
                    iy++;
                } else {
                    iy = 0;
                    rrY++;
                }
                checkForCancellation();
                pm.worked(1);
            }
        } finally {
            pm.done();
        }
    }


    public static class Spi extends OperatorSpi {
        public Spi() {
            super(RRToFRSOp.class);
        }
    }
}
