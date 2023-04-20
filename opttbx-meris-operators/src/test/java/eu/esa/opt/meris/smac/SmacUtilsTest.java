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
package eu.esa.opt.meris.smac;

import org.esa.snap.dataio.envisat.EnvisatConstants;
import org.junit.Test;

import static org.junit.Assert.*;

public class SmacUtilsTest {

    @Test
    public void testGetSensorTypeDoesNotAcceptNullParameter() {
        try {
            SmacUtils.getSensorType(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testGetSensorTypeReturnsCorrectType() {
        String type;

        type = SmacUtils.getSensorType(EnvisatConstants.AATSR_L1B_TOA_PRODUCT_TYPE_NAME);
        assertEquals(SensorCoefficientManager.AATSR_NAME, type);

        type = SmacUtils.getSensorType(EnvisatConstants.MERIS_FR_L1B_PRODUCT_TYPE_NAME);
        assertEquals(SensorCoefficientManager.MERIS_NAME, type);

        type = SmacUtils.getSensorType(EnvisatConstants.MERIS_RR_L1B_PRODUCT_TYPE_NAME);
        assertEquals(SensorCoefficientManager.MERIS_NAME, type);
    }

    @Test
    public void testGetSensorTypeIsNullOnIllegalTypes() {
        assertNull(SmacUtils.getSensorType("Nasenann"));
        assertNull(SmacUtils.getSensorType("strange"));
        assertNull(SmacUtils.getSensorType(""));
    }

    @Test
    public void testIsSupportedFileType() {
        try {
            SmacUtils.isSupportedProductType(null);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ignored) {
        }

        assertTrue(SmacUtils.isSupportedProductType(EnvisatConstants.AATSR_L1B_TOA_PRODUCT_TYPE_NAME));
        assertTrue(SmacUtils.isSupportedProductType(EnvisatConstants.MERIS_FR_L1B_PRODUCT_TYPE_NAME));
        assertTrue(SmacUtils.isSupportedProductType(EnvisatConstants.MERIS_RR_L1B_PRODUCT_TYPE_NAME));

        assertFalse(SmacUtils.isSupportedProductType("TomType"));
        assertFalse(SmacUtils.isSupportedProductType("NonExistingType"));
    }
}
