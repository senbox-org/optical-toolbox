/*
 * Copyright (C) 2015 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.avhrr.noaa.pod;

import org.esa.snap.core.datamodel.GeoPos;
import org.esa.snap.core.datamodel.PixelPos;
import org.esa.snap.core.util.math.CosineDistance;
import org.esa.snap.core.util.math.DistanceMeasure;

import javax.media.jai.PlanarImage;
import java.awt.Rectangle;
import java.awt.image.Raster;

import static java.lang.Math.*;

/**
 * A strategy for finding a pixel position for a given geographic position.
 *
 * @author Ralf Quast
 */
class PodPixelFinder {

    private static final int R = 128;
    private static final boolean ROUGH = false;

    private final PlanarImage lonImage;
    private final PlanarImage latImage;
    private final PlanarImage maskImage;
    private final double tolerance;
    private final int imageW;
    private final int imageH;

    PodPixelFinder(PlanarImage lonImage, PlanarImage latImage, PlanarImage maskImage,
                   double angularTolerance) {
        this.lonImage = lonImage;
        this.latImage = latImage;
        this.maskImage = maskImage;
        this.tolerance = 1.0 - Math.cos(Math.toRadians(angularTolerance));

        imageW = lonImage.getWidth();
        imageH = lonImage.getHeight();
    }

    /**
     * Returns the pixel position for a given geographic position.
     *
     * @param geoPos   the geographic position.
     * @param pixelPos the pixel position.
     */
    void findPixelPos(GeoPos geoPos, PixelPos pixelPos) {
        int x = (int) Math.floor(pixelPos.x);
        int y = (int) Math.floor(pixelPos.y);

        if (x < 0) {
            x = 0;
        } else if (x >= imageW) {
            x = imageW - 1;
        }
        if (y < 0) {
            y = 0;
        } else if (y >= imageH) {
            y = imageH - 1;
        }

        final int minX = max(x - R, 0);
        final int minY = max(y - R, 0);
        final int maxX = min(x + R, imageW - 1);
        final int maxY = min(y + R, imageH - 1);
        final Rectangle searchRegion = new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);

        final Raster lonData = lonImage.getData(searchRegion);
        final Raster latData = latImage.getData(searchRegion);
        final Raster maskData;
        if (maskImage != null) {
            maskData = maskImage.getData(searchRegion);
        } else {
            maskData = null;
        }

        final double lon = geoPos.lon;
        final double lat = geoPos.lat;
        final DistanceMeasure d = new CosineDistance(lon, lat);
        final Result result = new Result(lonData, latData, maskData, d, x, y, 2.0, false).invoke(x, y);

        for (int r = R; r > 0; r >>= 1) {
            final int midX = result.getX();
            final int midY = result.getY();

            final int outerMinX = max(minX, midX - r);
            final int outerMaxX = min(maxX, midX + r);
            final int outerMinY = max(minY, midY - r);
            final int outerMaxY = min(maxY, midY + r);

            // consider outer points in the N, S, E, and W
            result.invoke(outerMinX, midY);
            result.invoke(outerMaxX, midY);
            result.invoke(midX, outerMaxY);
            result.invoke(midX, outerMinY);
            // consider outer points in the NW, SW, SE, and NE
            result.invoke(outerMinX, outerMinY);
            result.invoke(outerMinX, outerMaxY);
            result.invoke(outerMaxX, outerMaxY);
            result.invoke(outerMaxX, outerMinY);

            //noinspection ConstantConditions,ConstantIfStatement
            if (ROUGH) {
                // consider inner points in the NW, SW, SE, and NE
                final int innerMinX = max(outerMinX, midX - (r >> 1));
                final int innerMaxX = min(outerMaxX, midX + (r >> 1));
                final int innerMinY = max(outerMinY, midY - (r >> 1));
                final int innerMaxY = min(outerMaxY, midY + (r >> 1));

                result.invoke(innerMinX, innerMinY);
                result.invoke(innerMinX, innerMaxY);
                result.invoke(innerMaxX, innerMaxY);
                result.invoke(innerMaxX, innerMinY);
            }
        }
        if (result.isFound()) {
            pixelPos.setLocation(result.getX() + 0.5f, result.getY() + 0.5f);
        } else {
            pixelPos.setInvalid();
        }
    }

    private final class Result {

        private final Raster lonData;
        private final Raster latData;
        private final Raster maskData;
        private final DistanceMeasure distanceMeasure;

        private int x;
        private int y;
        private double distance;
        private boolean found;

        public Result(Raster lonData, Raster latData, Raster maskData, DistanceMeasure distanceMeasure,
                      int x, int y, double distance, boolean found) {
            this.lonData = lonData;
            this.latData = latData;
            this.maskData = maskData;
            this.distanceMeasure = distanceMeasure;
            this.x = x;
            this.y = y;
            this.distance = distance;
            this.found = found;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean isFound() {
            return found;
        }

        public Result invoke(int otherX, int otherY) {
            if (maskData == null || maskData.getSample(otherX, otherY, 0) != 0) {
                final double lon = lonData.getSampleDouble(otherX, otherY, 0);
                final double lat = latData.getSampleDouble(otherX, otherY, 0);
                final double d = distanceMeasure.distance(lon, lat);

                if (d < distance) {
                    x = otherX;
                    y = otherY;
                    distance = d;
                    found = found || d < tolerance;
                }
            }
            return this;
        }
    }
}
