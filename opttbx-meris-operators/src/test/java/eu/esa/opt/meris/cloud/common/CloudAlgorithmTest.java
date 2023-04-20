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
package eu.esa.opt.meris.cloud.common;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class CloudAlgorithmTest {

    private CloudAlgorithm testAlgorithm;

    @Before
    public void setUp() throws Exception {
        URL auxdataDirUrl = getClass().getResource("/auxdata/cloud");
        Path auxdataDir = Paths.get(auxdataDirUrl.toURI());
        testAlgorithm = new CloudAlgorithm(auxdataDir, "nn_config_test.txt");
    }

    /*
      * Test method for 'org.esa.snap.processor.cloud.CloudAlgorithm.computeCloud(double[])'
      */
    @Test
    public void testComputeCloud() {
        final double[] in = new double[]{0.0778002, 0.0695650, 0.0591455, 0.0545394,
                0.0460968, 0.0415193, 0.0420742, 0.0421471,
                0.0421236, 0.293535, 1012.98, 762.190,
                0.622985, 0.996135, -0.0447822};

        double out = testAlgorithm.computeCloud(in);
        assertEquals("cloud NN result", 0.004993, out, 0.00001);
    }

    /*
      * Test method for 'eu.esa.opt.processor.cloud.CloudAlgorithm.nn2Probability(double)'
      */
    @Test
    public void testNn2Probability() {
        double probability = testAlgorithm.nn2Probability(0.004993);
        assertEquals("probability", 0.01313, probability, 0.00001);
    }

}
