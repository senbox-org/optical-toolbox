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

package eu.esa.opt.meris.cloud;

import eu.esa.opt.meris.ModuleActivator;
import org.esa.snap.core.util.math.MathUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CloudPNTest {

    private CloudPN cloudPn;

    @BeforeClass
    public static void beforeClass() {
        ModuleActivator.activate();
    }

    @Before
    public void setUp() throws IOException {
        Map<String, String> cloudConfig = new HashMap<>();
        cloudConfig.put(CloudPN.CONFIG_FILE_NAME, "cloud_config.txt");
        cloudPn = new CloudPN(CloudOperator.getAuxdataInstallationPath());
        cloudPn.setUp(cloudConfig);
    }

    @Test
    public void testAltitudeCorrectedPressure() {
        double pressure = 1000;
        double altitude = 100;
        double correctedPressure = cloudPn.altitudeCorrectedPressure(pressure, altitude, true);
        assertEquals("corrected pressure", 988.08, correctedPressure, 0.01);
        correctedPressure = cloudPn.altitudeCorrectedPressure(pressure, altitude, false);
        assertEquals("corrected pressure", 1000, correctedPressure, 0.0001);
    }

    @Test
    public void testCalculateI() {
        double radiance = 50;
        float sunSpectralFlux = 10;
        double sunZenith = 45;
        double i = cloudPn.calculateI(radiance, sunSpectralFlux, sunZenith);
        assertEquals("calculated i", (radiance / (sunSpectralFlux * Math.cos(sunZenith * MathUtils.DTOR))), i, 0.00001);
    }
}
