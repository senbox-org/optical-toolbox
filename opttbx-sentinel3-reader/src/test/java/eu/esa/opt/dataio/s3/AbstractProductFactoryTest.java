/*
 * Copyright (c) 2021.  Brockmann Consult GmbH (info@brockmann-consult.de)
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

package eu.esa.opt.dataio.s3;

import com.bc.ceres.annotation.STTM;
import eu.esa.opt.dataio.s3.AbstractProductFactory;
import eu.esa.opt.dataio.s3.slstr.SlstrLevel1ProductFactory;
import org.checkerframework.checker.units.qual.A;
import org.esa.snap.core.dataio.geocoding.forward.PixelForward;
import org.esa.snap.core.dataio.geocoding.forward.PixelInterpolatingForward;
import org.esa.snap.core.dataio.geocoding.inverse.PixelGeoIndexInverse;
import org.esa.snap.core.dataio.geocoding.inverse.PixelQuadTreeInverse;
import org.esa.snap.core.datamodel.ColorPaletteDef;
import org.junit.Test;

import java.awt.*;

import static eu.esa.opt.dataio.s3.slstr.SlstrLevel1ProductFactory.SLSTR_L1B_PIXEL_GEOCODING_INVERSE;
import static org.esa.snap.core.dataio.geocoding.ComponentGeoCoding.SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY;
import static org.junit.Assert.assertEquals;

public class AbstractProductFactoryTest {

    @Test
    public void testGetForwardAndInverseKeys_default() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.clearProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            System.clearProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

            final String[] keys = AbstractProductFactory.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelForward.KEY, keys[0]);
            assertEquals(PixelQuadTreeInverse.KEY, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    public void testGetForwardAndInverseKeys_interpolating() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, "true");

            final String[] keys = SlstrLevel1ProductFactory.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelInterpolatingForward.KEY, keys[0]);
            assertEquals(PixelQuadTreeInverse.KEY_INTERPOLATING, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    public void testGetForwardAndInverseKeys_inverse() {
        final String inverseKey = System.getProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
        final String fractionalAccuracy = System.getProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);

        try {
            System.clearProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY);
            System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, PixelGeoIndexInverse.KEY);

            final String[] keys = SlstrLevel1ProductFactory.getForwardAndInverseKeys_pixelCoding(SLSTR_L1B_PIXEL_GEOCODING_INVERSE);
            assertEquals(PixelForward.KEY, keys[0]);
            assertEquals(PixelGeoIndexInverse.KEY, keys[1]);
        } finally {
            if (inverseKey != null) {
                System.setProperty(SLSTR_L1B_PIXEL_GEOCODING_INVERSE, inverseKey);
            }
            if (fractionalAccuracy != null) {
                System.setProperty(SYSPROP_SNAP_PIXEL_CODING_FRACTION_ACCURACY, fractionalAccuracy);
            }
        }
    }

    @Test
    @STTM("SNAP-3545")
    public void testGetCounterWaterColorPalette() {
        final ColorPaletteDef colorPalette = AbstractProductFactory.getCounterWaterColorPalette();
        assertEquals(8, colorPalette.getNumPoints());

        ColorPaletteDef.Point point = colorPalette.getPointAt(0);
        assertEquals(0.0, point.getSample(), 1e-8);
        assertEquals(new Color(145, 70, 15), point.getColor());

        point = colorPalette.getPointAt(1);
        assertEquals(1.0, point.getSample(), 1e-8);
        assertEquals(new Color(145, 110, 15), point.getColor());

        point = colorPalette.getPointAt(2);
        assertEquals(2.0, point.getSample(), 1e-8);
        assertEquals(new Color(185, 160, 50), point.getColor());

        point = colorPalette.getPointAt(3);
        assertEquals(3.0, point.getSample(), 1e-8);
        assertEquals(new Color(135, 145, 105), point.getColor());

        point = colorPalette.getPointAt(4);
        assertEquals(4.0, point.getSample(), 1e-8);
        assertEquals(new Color(130, 160, 95), point.getColor());

        point = colorPalette.getPointAt(5);
        assertEquals(5.0, point.getSample(), 1e-8);
        assertEquals(new Color(95, 160, 120), point.getColor());

        point = colorPalette.getPointAt(6);
        assertEquals(6.0, point.getSample(), 1e-8);
        assertEquals(new Color(25, 140, 180), point.getColor());

        point = colorPalette.getPointAt(7);
        assertEquals(7.0, point.getSample(), 1e-8);
        assertEquals(new Color(0, 0, 0), point.getColor());
    }
}
