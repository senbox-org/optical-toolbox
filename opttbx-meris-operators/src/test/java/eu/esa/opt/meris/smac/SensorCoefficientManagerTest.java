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

import eu.esa.opt.meris.ModuleActivator;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SensorCoefficientManagerTest {

    private static Path auxdataDir;

    @BeforeClass
    public static void beforeClass() {
        ModuleActivator.activate();
        auxdataDir = SmacOperator.getAuxdataInstallDir();
    }


    @Test
    public void testSensorCoefficientManagerWithNoArgs() throws IOException {
        SensorCoefficientManager mgr = new SensorCoefficientManager();

        // when using default constructor, no file shall be returned upon request
        assertNull(mgr.getCoefficientFile("MERIS", "radiance_1", AEROSOL_TYPE.CONTINENTAL));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSensorCoefficientManagerWithNull() throws IOException {
        // the URL constructor shall not accept a null argument
        new SensorCoefficientManager(null);
    }

    @Test
    public void testSensorCoefficientManagerWithValidURL() throws IOException {
        // when inserting a valid url, we shall retrieve a coefficient file
        SensorCoefficientManager mgr = new SensorCoefficientManager(auxdataDir.toUri().toURL());
        assertNotNull(mgr.getCoefficientFile("MERIS", "radiance_2", AEROSOL_TYPE.DESERT));

    }

    @Test
    public void testGetCoefficientFile() throws IOException {
        SensorCoefficientManager mgr = new SensorCoefficientManager(auxdataDir.toUri().toURL());
        assertNotNull(mgr.getCoefficientFile("MERIS", "radiance_2", AEROSOL_TYPE.DESERT));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetURLWithNull() throws IOException {
        // it must not be possible to set a null argument
        SensorCoefficientManager mgr = new SensorCoefficientManager();
        mgr.setURL(null);
    }

    @Test
    public void testSetURLWithValidUrl() throws IOException {
        SensorCoefficientManager mgr = new SensorCoefficientManager();

        // if we set a valid url - return something when we ask for it :-)
        URL url = auxdataDir.toUri().toURL();
        mgr.setURL(url);
        assertNotNull(mgr.getCoefficientFile("MERIS", "radiance_3", AEROSOL_TYPE.CONTINENTAL));
    }

}
